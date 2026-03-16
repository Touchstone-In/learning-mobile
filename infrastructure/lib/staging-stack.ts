import * as cdk from "aws-cdk-lib";
import * as ec2 from "aws-cdk-lib/aws-ec2";
import * as ecs from "aws-cdk-lib/aws-ecs";
import * as ecr from "aws-cdk-lib/aws-ecr";
import * as efs from "aws-cdk-lib/aws-efs";
import * as elbv2 from "aws-cdk-lib/aws-elasticloadbalancingv2";
import * as logs from "aws-cdk-lib/aws-logs";
import * as iam from "aws-cdk-lib/aws-iam";
import * as ssm from "aws-cdk-lib/aws-ssm";
import { Construct } from "constructs";

export class StagingStack extends cdk.Stack {
  public readonly albDnsName: cdk.CfnOutput;
  public readonly ecrRepoUri: cdk.CfnOutput;

  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    // ── ECR Repository ──────────────────────────────────────────
    const repo = new ecr.Repository(this, "LearningBackendRepo", {
      repositoryName: "tsin-learning-backend-staging",
      removalPolicy: cdk.RemovalPolicy.RETAIN,
      lifecycleRules: [
        { maxImageCount: 10, description: "Keep last 10 images" },
      ],
    });

    // ── VPC ─────────────────────────────────────────────────────
    const vpc = new ec2.Vpc(this, "StagingVpc", {
      maxAzs: 2,
      natGateways: 1,
      subnetConfiguration: [
        {
          cidrMask: 24,
          name: "Public",
          subnetType: ec2.SubnetType.PUBLIC,
        },
        {
          cidrMask: 24,
          name: "Private",
          subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS,
        },
      ],
    });

    // ── EFS for MongoDB data persistence ────────────────────────
    const mongoFileSystem = new efs.FileSystem(this, "MongoEfs", {
      vpc,
      lifecyclePolicy: efs.LifecyclePolicy.AFTER_30_DAYS,
      performanceMode: efs.PerformanceMode.GENERAL_PURPOSE,
      removalPolicy: cdk.RemovalPolicy.RETAIN,
      encrypted: true,
    });

    const mongoAccessPoint = mongoFileSystem.addAccessPoint("MongoData", {
      path: "/mongodata",
      posixUser: { uid: "999", gid: "999" },       // mongodb user
      createAcl: { ownerUid: "999", ownerGid: "999", permissions: "755" },
    });

    // ── ECS Cluster ─────────────────────────────────────────────
    const cluster = new ecs.Cluster(this, "StagingCluster", {
      clusterName: "tsin-learning-staging",
      vpc,
      containerInsights: true,
    });

    // ── Secrets from SSM Parameter Store ────────────────────────
    const jwtSecret = ssm.StringParameter.fromSecureStringParameterAttributes(
      this, "JwtSecret", { parameterName: "/tsin/staging/JWT_SECRET" }
    );
    const awsAccessKey = ssm.StringParameter.fromSecureStringParameterAttributes(
      this, "AwsAccessKey", { parameterName: "/tsin/staging/AWS_ACCESS_KEY" }
    );
    const awsSecretKey = ssm.StringParameter.fromSecureStringParameterAttributes(
      this, "AwsSecretKey", { parameterName: "/tsin/staging/AWS_SECRET_KEY" }
    );
    const mongoRootPassword = ssm.StringParameter.fromSecureStringParameterAttributes(
      this, "MongoRootPassword", { parameterName: "/tsin/staging/MONGO_ROOT_PASSWORD" }
    );

    // ── Task Definition ─────────────────────────────────────────
    const taskDef = new ecs.FargateTaskDefinition(this, "BackendTaskDef", {
      memoryLimitMiB: 2048,
      cpu: 1024,
      runtimePlatform: {
        cpuArchitecture: ecs.CpuArchitecture.X86_64,
        operatingSystemFamily: ecs.OperatingSystemFamily.LINUX,
      },
    });

    // Add EFS volume for MongoDB persistence
    taskDef.addVolume({
      name: "mongo-data",
      efsVolumeConfiguration: {
        fileSystemId: mongoFileSystem.fileSystemId,
        transitEncryption: "ENABLED",
        authorizationConfig: {
          accessPointId: mongoAccessPoint.accessPointId,
          iam: "ENABLED",
        },
      },
    });

    // Grant task role access to EFS
    mongoFileSystem.grant(
      taskDef.taskRole,
      "elasticfilesystem:ClientMount",
      "elasticfilesystem:ClientWrite",
      "elasticfilesystem:ClientRootAccess"
    );

    const backendLogGroup = new logs.LogGroup(this, "BackendLogs", {
      logGroupName: "/ecs/tsin-learning-staging/backend",
      retention: logs.RetentionDays.TWO_WEEKS,
      removalPolicy: cdk.RemovalPolicy.DESTROY,
    });

    const mongoLogGroup = new logs.LogGroup(this, "MongoLogs", {
      logGroupName: "/ecs/tsin-learning-staging/mongodb",
      retention: logs.RetentionDays.TWO_WEEKS,
      removalPolicy: cdk.RemovalPolicy.DESTROY,
    });

    // ── MongoDB Sidecar Container ───────────────────────────────
    const mongoContainer = taskDef.addContainer("mongodb", {
      image: ecs.ContainerImage.fromRegistry("mongo:7"),
      memoryLimitMiB: 768,
      cpu: 256,
      essential: true,
      logging: ecs.LogDrivers.awsLogs({
        streamPrefix: "mongodb",
        logGroup: mongoLogGroup,
      }),
      environment: {
        MONGO_INITDB_DATABASE: "tsin_learning",
      },
      secrets: {
        MONGO_INITDB_ROOT_USERNAME: ecs.Secret.fromSsmParameter(
          ssm.StringParameter.fromSecureStringParameterAttributes(
            this, "MongoRootUser", { parameterName: "/tsin/staging/MONGO_ROOT_USERNAME" }
          )
        ),
        MONGO_INITDB_ROOT_PASSWORD: ecs.Secret.fromSsmParameter(mongoRootPassword),
      },
      healthCheck: {
        command: [
          "CMD-SHELL",
          "mongosh --eval 'db.adminCommand(\"ping\")' --quiet || exit 1",
        ],
        interval: cdk.Duration.seconds(30),
        timeout: cdk.Duration.seconds(10),
        retries: 5,
        startPeriod: cdk.Duration.seconds(40),
      },
    });

    mongoContainer.addPortMappings({ containerPort: 27017 });
    mongoContainer.addMountPoints({
      sourceVolume: "mongo-data",
      containerPath: "/data/db",
      readOnly: false,
    });

    // ── Backend Container ───────────────────────────────────────
    // MONGO_URI uses localhost because MongoDB runs as a sidecar
    // in the same task definition (shared network namespace)
    const backendContainer = taskDef.addContainer("backend", {
      image: ecs.ContainerImage.fromEcrRepository(repo, "latest"),
      memoryLimitMiB: 1024,
      cpu: 512,
      essential: true,
      logging: ecs.LogDrivers.awsLogs({
        streamPrefix: "backend",
        logGroup: backendLogGroup,
      }),
      environment: {
        NODE_ENV: "staging",
        PORT: "8003",
        AWS_DEFAULT_REGION: "ca-central-1",
        // MongoDB runs as sidecar — Prisma connects via localhost
        MONGO_URI: "mongodb://admin:changeme@localhost:27017/tsin_learning?authSource=admin",
        USER_API_URL: "https://zxkbbj3pcy.us-east-1.awsapprunner.com/api",
        AUTH_URL: "https://zxkbbj3pcy.us-east-1.awsapprunner.com",
        FRONTEND_URL: "https://portal.tsin.ca",
        S3_BUCKET: "touchstone-be",
      },
      secrets: {
        JWT_SECRET: ecs.Secret.fromSsmParameter(jwtSecret),
        AWS_ACCESS_KEY: ecs.Secret.fromSsmParameter(awsAccessKey),
        AWS_SECRET_KEY: ecs.Secret.fromSsmParameter(awsSecretKey),
      },
      healthCheck: {
        command: [
          "CMD-SHELL",
          "wget -qO- http://localhost:8003/api/health || exit 1",
        ],
        interval: cdk.Duration.seconds(30),
        timeout: cdk.Duration.seconds(5),
        retries: 3,
        startPeriod: cdk.Duration.seconds(60),
      },
    });

    backendContainer.addPortMappings({ containerPort: 8003 });

    // Backend depends on MongoDB being healthy
    backendContainer.addContainerDependencies({
      container: mongoContainer,
      condition: ecs.ContainerDependencyCondition.HEALTHY,
    });

    // ── ALB ─────────────────────────────────────────────────────
    const alb = new elbv2.ApplicationLoadBalancer(this, "StagingAlb", {
      vpc,
      internetFacing: true,
      loadBalancerName: "tsin-learning-staging-alb",
    });

    const listener = alb.addListener("HttpListener", {
      port: 80,
      protocol: elbv2.ApplicationProtocol.HTTP,
    });

    // ── Fargate Service ─────────────────────────────────────────
    const serviceSg = new ec2.SecurityGroup(this, "ServiceSg", {
      vpc,
      description: "Security group for backend + mongodb task",
      allowAllOutbound: true,
    });

    // Allow EFS access from Fargate tasks
    mongoFileSystem.connections.allowDefaultPortFrom(serviceSg, "EFS from Fargate");

    const service = new ecs.FargateService(this, "BackendService", {
      cluster,
      taskDefinition: taskDef,
      desiredCount: 1,
      assignPublicIp: false,
      serviceName: "tsin-learning-backend-staging",
      circuitBreaker: { rollback: true },
      minHealthyPercent: 100,
      maxHealthyPercent: 200,
      securityGroups: [serviceSg],
      platformVersion: ecs.FargatePlatformVersion.LATEST,
    });

    listener.addTargets("BackendTarget", {
      port: 8003,
      protocol: elbv2.ApplicationProtocol.HTTP,
      targets: [service],
      healthCheck: {
        path: "/api/health",
        interval: cdk.Duration.seconds(30),
        timeout: cdk.Duration.seconds(5),
        healthyThresholdCount: 2,
        unhealthyThresholdCount: 3,
      },
    });

    // Allow ALB to reach backend
    service.connections.allowFrom(alb, ec2.Port.tcp(8003), "ALB to backend");
    // Allow outbound for external APIs
    service.connections.allowToAnyIpv4(ec2.Port.tcp(443), "HTTPS outbound");

    // ── Outputs ───────────────────────────────────────────────────
    this.albDnsName = new cdk.CfnOutput(this, "AlbDnsName", {
      value: alb.loadBalancerDnsName,
      description: "Staging ALB DNS name — use as mobile API_BASE_URL",
      exportName: "TsinLearningStagingAlbDns",
    });

    this.ecrRepoUri = new cdk.CfnOutput(this, "EcrRepoUri", {
      value: repo.repositoryUri,
      description: "ECR repository URI for Docker push",
      exportName: "TsinLearningStagingEcrUri",
    });

    new cdk.CfnOutput(this, "ClusterName", {
      value: cluster.clusterName,
      description: "ECS Cluster name",
    });

    new cdk.CfnOutput(this, "ServiceName", {
      value: service.serviceName,
      description: "ECS Service name",
    });

    new cdk.CfnOutput(this, "MongoEfsId", {
      value: mongoFileSystem.fileSystemId,
      description: "EFS file system ID for MongoDB data",
    });
  }
}


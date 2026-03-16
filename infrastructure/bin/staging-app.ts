#!/usr/bin/env node
import "source-map-support/register";
import * as cdk from "aws-cdk-lib";
import { StagingStack } from "../lib/staging-stack";

const app = new cdk.App();

new StagingStack(app, "TsinLearningStagingStack", {
  env: {
    account: process.env.CDK_DEFAULT_ACCOUNT,
    region: process.env.CDK_DEFAULT_REGION || "ca-central-1",
  },
  description:
    "TSIN Learning Backend - Staging Environment (ECS Fargate + ALB)",
});

app.synth();


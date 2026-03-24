import { useCallback, useEffect, useState } from 'react';
import { ScrollView, StyleSheet, View } from 'react-native';
import { Text, Button, useTheme } from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { mobileApi } from '../../src/api/mobile';
import { userApi } from '../../src/api/user';
import type { LearnerOverviewResponse, KeyDateSummary, NextSessionSummary, UserDto } from '../../src/types/api';
import {
  GuidanceSectionHeader,
  GuidanceSectionCard,
  GuidanceBanner,
  GuidanceStatusChip,
  GuidanceLoadingState,
  GuidanceStatePanel,
} from '../../src/components/GuidanceComponents';

export default function HomeScreen() {
  const theme = useTheme();
  const router = useRouter();

  const [user, setUser] = useState<UserDto | null>(null);
  const [overview, setOverview] = useState<LearnerOverviewResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const loadHome = useCallback(async (quiet = false) => {
    if (!quiet) setIsLoading(true);
    else setIsRefreshing(true);
    setErrorMessage(null);
    try {
      const [u, o] = await Promise.all([userApi.getMe(), mobileApi.getOverview()]);
      setUser(u);
      setOverview(o);
    } catch (e: any) {
      setErrorMessage(e?.response?.data?.message ?? 'Unable to load your overview.');
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  }, []);

  useEffect(() => { loadHome(); }, [loadHome]);

  const hasContent = user !== null || overview !== null;
  const greeting = user?.firstName ? `Welcome back, ${user.firstName}` : 'Welcome back';

  if (!hasContent && isLoading) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]}>
        <ScrollView contentContainerStyle={styles.scroll}>
          <GuidanceLoadingState message="Getting your learning snapshot ready..." />
        </ScrollView>
      </SafeAreaView>
    );
  }

  if (!hasContent && errorMessage) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]}>
        <GuidanceStatePanel
          title="Home is not available right now"
          message={`${errorMessage} Try again to load your current overview.`}
          actionLabel="Retry"
          onAction={() => loadHome()}
        />
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]}>
      <ScrollView contentContainerStyle={styles.scroll}>
        <GuidanceSectionHeader
          title={greeting}
          supportingText={isRefreshing ? 'Refreshing…' : "Here's your current snapshot."}
          action={
            <MaterialCommunityIcons
              name="refresh"
              size={24}
              color={theme.colors.onSurfaceVariant}
              onPress={() => loadHome(true)}
            />
          }
        />

        {errorMessage && (
          <GuidanceBanner title="Refresh incomplete" message={errorMessage} tone="warning" />
        )}

        {overview?.nextSession
          ? <NextSessionHero session={overview.nextSession} onOpenSchedule={() => router.push('/(app)/schedule')} />
          : <NoSessionCard onOpenSchedule={() => router.push('/(app)/schedule')} />
        }

        {overview && <ProgramOverviewCard overview={overview} />}

        {(overview?.keyDates ?? []).length > 0 && (
          <KeyDatesCard dates={overview!.keyDates!} />
        )}
      </ScrollView>
    </SafeAreaView>
  );
}

function NextSessionHero({ session, onOpenSchedule }: { session: NextSessionSummary; onOpenSchedule: () => void }) {
  return (
    <GuidanceSectionCard
      title="Next session"
      supportingText={session.sessionName ?? 'Details below'}
      action={<Button mode="contained-tonal" onPress={onOpenSchedule} compact>Full schedule</Button>}
    >
      <View style={styles.chipRow}>
        {session.day && <GuidanceStatusChip label={session.day} tone="brand" />}
        {session.period && <GuidanceStatusChip label={session.period} tone="neutral" />}
      </View>
      {(session.track || session.group) && (
        <View style={styles.chipRow}>
          {session.track && <GuidanceStatusChip label={session.track} tone="neutral" />}
          {session.group && <GuidanceStatusChip label={session.group} tone="neutral" />}
        </View>
      )}
    </GuidanceSectionCard>
  );
}

function NoSessionCard({ onOpenSchedule }: { onOpenSchedule: () => void }) {
  return (
    <GuidanceSectionCard
      title="No upcoming session"
      supportingText="There are no scheduled sessions right now."
      action={<Button mode="contained-tonal" onPress={onOpenSchedule} compact>View schedule</Button>}
    >
      <Text variant="bodyMedium">Check the full schedule or wait for new sessions to be published.</Text>
    </GuidanceSectionCard>
  );
}

function ProgramOverviewCard({ overview }: { overview: LearnerOverviewResponse }) {
  return (
    <GuidanceSectionCard title={overview.programName ?? 'Your program'} supportingText={overview.programType}>
      {overview.applicationStatus && (
        <View style={styles.statusRow}>
          <Text variant="bodyMedium">Application</Text>
          <GuidanceStatusChip label={overview.applicationStatus} tone="neutral" />
        </View>
      )}
      {overview.registrationStatus && (
        <View style={styles.statusRow}>
          <Text variant="bodyMedium">Registration</Text>
          <GuidanceStatusChip label={overview.registrationStatus} tone="neutral" />
        </View>
      )}
    </GuidanceSectionCard>
  );
}

function KeyDatesCard({ dates }: { dates: KeyDateSummary[] }) {
  return (
    <GuidanceSectionCard title="Key dates" supportingText={`${dates.length} upcoming`}>
      {dates.map((kd) => (
        <View key={kd.label + kd.date} style={styles.statusRow}>
          <Text variant="bodyMedium" style={{ fontWeight: '500' }}>{kd.label}</Text>
          <Text variant="bodyMedium">{kd.date}</Text>
        </View>
      ))}
    </GuidanceSectionCard>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  scroll: { padding: 16, gap: 12 },
  chipRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 6, marginTop: 4 },
  statusRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 4 },
});


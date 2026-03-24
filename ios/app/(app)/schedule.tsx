import { useCallback, useEffect, useState } from 'react';
import { FlatList, ScrollView, StyleSheet, View } from 'react-native';
import { Text, useTheme } from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { mobileApi } from '../../src/api/mobile';
import type { ScheduleDay, ScheduleWeek } from '../../src/types/api';
import {
  GuidanceSectionHeader,
  GuidanceSectionCard,
  GuidanceBanner,
  GuidanceStatusChip,
  GuidanceLoadingState,
  GuidanceStatePanel,
} from '../../src/components/GuidanceComponents';

export default function ScheduleScreen() {
  const theme = useTheme();
  const [weeks, setWeeks] = useState<ScheduleWeek[]>([]);
  const [weekLabel, setWeekLabel] = useState<string | null>(null);
  const [lastUpdated, setLastUpdated] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [isCached, setIsCached] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const loadSchedule = useCallback(async (quiet = false) => {
    if (!quiet) setIsLoading(true);
    else setIsRefreshing(true);
    setErrorMessage(null);
    try {
      const data = await mobileApi.getSchedule();
      setWeeks(data.weeks ?? []);
      setLastUpdated(data.lastUpdated ? data.lastUpdated.substring(0, 10) : null);
      // Determine week label from first week
      if ((data.weeks ?? []).length > 0) {
        setWeekLabel(data.weeks[0].weekName);
      }
      setIsCached(false);
    } catch (e: any) {
      setErrorMessage(e?.response?.data?.message ?? 'Unable to load your schedule.');
      setIsCached(weeks.length > 0);
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  }, [weeks.length]);

  useEffect(() => { loadSchedule(); }, []);

  const hasContent = weeks.length > 0;

  if (!hasContent && isLoading) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]}>
        <ScrollView contentContainerStyle={styles.scroll}>
          <GuidanceLoadingState message="Loading your schedule..." />
          <GuidanceSectionCard
            title="Getting this week ready"
            supportingText="Organizing your sessions into an easy-to-scan view."
          >
            <Text variant="bodyMedium" style={{ color: theme.colors.onSurfaceVariant }}>
              Your day-by-day session details will appear here once the sync finishes.
            </Text>
          </GuidanceSectionCard>
        </ScrollView>
      </SafeAreaView>
    );
  }

  if (!hasContent && errorMessage) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]}>
        <GuidanceStatePanel
          title="Schedule unavailable"
          message={`${errorMessage} Retry to load the latest session plan.`}
          actionLabel="Retry"
          onAction={() => loadSchedule()}
        />
      </SafeAreaView>
    );
  }

  if (!hasContent) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]}>
        <GuidanceStatePanel
          title="No sessions scheduled"
          message="When your training calendar is ready, it will appear here organized by week."
          actionLabel="Refresh"
          onAction={() => loadSchedule()}
        />
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]}>
      <FlatList
        data={weeks}
        keyExtractor={(item) => item.weekName}
        contentContainerStyle={styles.scroll}
        ListHeaderComponent={
          <>
            <GuidanceSectionHeader
              title={weekLabel ?? 'Your schedule'}
              supportingText={
                isRefreshing ? 'Refreshing your schedule...' :
                lastUpdated ? `Last updated ${lastUpdated}` :
                'Scan your upcoming sessions at a glance.'
              }
              action={
                <MaterialCommunityIcons
                  name="refresh"
                  size={24}
                  color={theme.colors.onSurfaceVariant}
                  onPress={() => loadSchedule(true)}
                />
              }
            />
            {(isCached || errorMessage) && (
              <GuidanceBanner
                title={isCached ? 'Offline schedule' : 'Refresh incomplete'}
                message={errorMessage ?? 'Showing the last saved schedule while you\'re offline.'}
                tone="warning"
              />
            )}
          </>
        }
        renderItem={({ item }) => <WeekCard week={item} />}
        ItemSeparatorComponent={() => <View style={{ height: 12 }} />}
      />
    </SafeAreaView>
  );
}

function WeekCard({ week }: { week: ScheduleWeek }) {
  const theme = useTheme();
  return (
    <GuidanceSectionCard
      title={week.weekName}
      supportingText={week.days.length === 1 ? '1 session' : `${week.days.length} sessions`}
    >
      {week.days.length === 0 ? (
        <Text variant="bodyMedium" style={{ color: theme.colors.onSurfaceVariant }}>
          No sessions scheduled for this week.
        </Text>
      ) : (
        week.days.map((day, idx) => (
          <View key={`${day.day}-${idx}`}>
            <SessionRow day={day} />
            {idx < week.days.length - 1 && (
              <View style={[styles.divider, { backgroundColor: theme.colors.outlineVariant }]} />
            )}
          </View>
        ))
      )}
    </GuidanceSectionCard>
  );
}

function SessionRow({ day }: { day: ScheduleDay }) {
  const theme = useTheme();
  return (
    <View style={[styles.sessionRow, { backgroundColor: theme.colors.surfaceContainerLow }]}>
      <Text variant="titleSmall" style={{ color: theme.colors.onSurface, fontWeight: '600' }}>
        {day.session.sessionName}
      </Text>
      <Text variant="bodySmall" style={{ color: theme.colors.onSurfaceVariant, marginTop: 2 }}>
        {day.day} · {day.period}
      </Text>
      <View style={styles.chipRow}>
        <GuidanceStatusChip label={day.session.track} tone="neutral" />
        <GuidanceStatusChip label={day.session.group} tone="neutral" />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  scroll: { padding: 16, gap: 12 },
  sessionRow: { borderRadius: 12, padding: 12, marginVertical: 4 },
  chipRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 6, marginTop: 8 },
  divider: { height: 1, marginVertical: 4 },
});


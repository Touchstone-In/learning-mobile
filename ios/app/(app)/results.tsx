import { useCallback, useEffect, useState } from 'react';
import { ScrollView, StyleSheet, View } from 'react-native';
import { Text, Button, useTheme } from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { mobileApi } from '../../src/api/mobile';
import type { LearnerResultSummary } from '../../src/types/api';
import {
  GuidanceSectionHeader,
  GuidanceSectionCard,
  GuidanceBanner,
  GuidanceStatusChip,
  GuidanceLoadingState,
  GuidanceStatePanel,
} from '../../src/components/GuidanceComponents';

export default function ResultsScreen() {
  const theme = useTheme();
  const router = useRouter();

  const [results, setResults] = useState<LearnerResultSummary[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const loadResults = useCallback(async (quiet = false) => {
    if (!quiet) setIsLoading(true);
    else setIsRefreshing(true);
    setErrorMessage(null);
    try {
      const data = await mobileApi.getResults();
      setResults(data.results ?? []);
    } catch (e: any) {
      setErrorMessage(e?.response?.data?.message ?? 'Unable to load your results.');
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  }, []);

  useEffect(() => { loadResults(); }, []);

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]}>
      <ScrollView contentContainerStyle={styles.scroll}>
        <GuidanceSectionHeader
          title="Your results"
          supportingText={isRefreshing ? 'Refreshing…' : 'Published results and program updates.'}
          action={
            <MaterialCommunityIcons
              name="refresh"
              size={24}
              color={theme.colors.onSurfaceVariant}
              onPress={() => loadResults(true)}
            />
          }
        />

        {errorMessage && (
          <GuidanceBanner title="Couldn't load results" message={errorMessage} tone="warning" />
        )}

        {isLoading ? (
          <GuidanceLoadingState message="Loading your results…" />
        ) : results.length === 0 && !errorMessage ? (
          <GuidanceStatePanel
            title="You're all caught up"
            message="When your results are released or important updates arrive, they'll appear here."
            actionLabel="Manage alert preferences"
            onAction={() => router.push('/(app)/settings')}
          />
        ) : results.length > 0 ? (
          <GuidanceSectionCard
            title="Released results"
            supportingText="Your published program results are shown below."
            action={
              <GuidanceStatusChip
                label={`${results.length} result${results.length !== 1 ? 's' : ''}`}
                tone="brand"
              />
            }
          >
            {results.map((result) => (
              <ResultRow key={result.id} result={result} />
            ))}
            <View style={styles.portalNote}>
              <MaterialCommunityIcons
                name="information-outline"
                size={14}
                color={theme.colors.onSurfaceVariant}
              />
              <Text variant="bodySmall" style={[styles.portalNoteText, { color: theme.colors.onSurfaceVariant }]}>
                Log in to the portal at learn.tsin.ca to download your results.
              </Text>
            </View>
            <Button
              mode="contained-tonal"
              onPress={() => router.push('/(app)/settings')}
              style={styles.prefsButton}
            >
              Manage alert preferences
            </Button>
          </GuidanceSectionCard>
        ) : null}
      </ScrollView>
    </SafeAreaView>
  );
}

function ResultRow({ result }: { result: LearnerResultSummary }) {
  const theme = useTheme();
  const statusTone = result.attendanceStatus?.toUpperCase() === 'COMPLETE'
    ? 'success' as const
    : result.attendanceStatus?.toUpperCase() === 'INCOMPLETE'
    ? 'warning' as const
    : 'neutral' as const;

  return (
    <View style={[styles.resultRow, { backgroundColor: theme.colors.surfaceContainerLow }]}>
      <View style={styles.resultHeader}>
        <View style={{ flex: 1, marginRight: 8 }}>
          <Text variant="titleSmall" style={{ color: theme.colors.onSurface, fontWeight: '600' }}>
            {result.name ?? 'Program Result'}
          </Text>
          {result.postgraduateTrainingProgram && (
            <Text variant="bodySmall" style={{ color: theme.colors.onSurfaceVariant, marginTop: 2 }}>
              {result.postgraduateTrainingProgram}
            </Text>
          )}
        </View>
        <GuidanceStatusChip label={result.attendanceStatus ?? 'Released'} tone={statusTone} />
      </View>
      {result.comment && (
        <Text variant="bodyMedium" style={{ color: theme.colors.onSurface, marginTop: 8 }}>
          {result.comment}
        </Text>
      )}
      {result.publishedAt && (
        <Text variant="bodySmall" style={{ color: theme.colors.onSurfaceVariant, marginTop: 6 }}>
          Released: {result.publishedAt.substring(0, 10)}
        </Text>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  scroll: { padding: 16, gap: 12 },
  resultRow: { borderRadius: 12, padding: 12, marginVertical: 4 },
  resultHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' },
  portalNote: { flexDirection: 'row', alignItems: 'center', gap: 6, marginTop: 12 },
  portalNoteText: { flex: 1, lineHeight: 18 },
  prefsButton: { borderRadius: 24, marginTop: 8 },
});


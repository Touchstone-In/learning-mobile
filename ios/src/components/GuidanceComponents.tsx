import React from 'react';
import { StyleSheet, View } from 'react-native';
import { ActivityIndicator, Button, Card, Chip, Text, useTheme } from 'react-native-paper';

// ── Tones ────────────────────────────────────────────────────────────────────

export type GuidanceTone = 'neutral' | 'warning' | 'success' | 'critical' | 'brand';

function useToneColors(tone: GuidanceTone) {
  const theme = useTheme();
  switch (tone) {
    case 'success':  return { bg: '#D7F2E1', text: '#1F6A47', border: '#1F6A47' };
    case 'warning':  return { bg: '#FFEDBF', text: '#8A6116', border: '#8A6116' };
    case 'critical': return { bg: '#FCE8E6', text: '#D93025', border: '#D93025' };
    case 'brand':    return { bg: '#D6E3FF', text: '#002D74', border: '#002D74' };
    default:         return { bg: theme.colors.surfaceVariant, text: theme.colors.onSurfaceVariant, border: theme.colors.outline };
  }
}

// ── GuidanceSectionCard ──────────────────────────────────────────────────────

interface GuidanceSectionCardProps {
  title: string;
  supportingText?: string | null;
  children?: React.ReactNode;
  action?: React.ReactNode;
}

export function GuidanceSectionCard({ title, supportingText, children, action }: GuidanceSectionCardProps) {
  const theme = useTheme();
  return (
    <Card style={[styles.card, { backgroundColor: theme.colors.surface }]} elevation={0} mode="outlined">
      <Card.Content style={styles.cardContent}>
        <View style={styles.cardHeader}>
          <View style={styles.cardHeaderText}>
            <Text variant="titleMedium" style={{ color: theme.colors.onSurface, fontWeight: '600' }}>
              {title}
            </Text>
            {supportingText ? (
              <Text variant="bodySmall" style={{ color: theme.colors.onSurfaceVariant, marginTop: 2 }}>
                {supportingText}
              </Text>
            ) : null}
          </View>
          {action ? <View>{action}</View> : null}
        </View>
        {children ? <View style={styles.cardBody}>{children}</View> : null}
      </Card.Content>
    </Card>
  );
}

// ── GuidanceSectionHeader ────────────────────────────────────────────────────

interface GuidanceSectionHeaderProps {
  title: string;
  supportingText?: string;
  action?: React.ReactNode;
}

export function GuidanceSectionHeader({ title, supportingText, action }: GuidanceSectionHeaderProps) {
  const theme = useTheme();
  return (
    <View style={styles.sectionHeader}>
      <View style={{ flex: 1 }}>
        <Text variant="headlineSmall" style={{ color: theme.colors.onSurface, fontWeight: '700' }}>
          {title}
        </Text>
        {supportingText ? (
          <Text variant="bodyMedium" style={{ color: theme.colors.onSurfaceVariant, marginTop: 4 }}>
            {supportingText}
          </Text>
        ) : null}
      </View>
      {action}
    </View>
  );
}

// ── GuidanceBanner ───────────────────────────────────────────────────────────

interface GuidanceBannerProps {
  title: string;
  message: string;
  tone?: GuidanceTone;
}

export function GuidanceBanner({ title, message, tone = 'neutral' }: GuidanceBannerProps) {
  const colors = useToneColors(tone);
  return (
    <View style={[styles.banner, { backgroundColor: colors.bg, borderLeftColor: colors.border }]}>
      <Text variant="labelMedium" style={{ color: colors.text, fontWeight: '700' }}>{title}</Text>
      <Text variant="bodySmall" style={{ color: colors.text, marginTop: 2 }}>{message}</Text>
    </View>
  );
}

// ── GuidanceStatusChip ───────────────────────────────────────────────────────

interface GuidanceStatusChipProps {
  label: string;
  tone?: GuidanceTone;
}

export function GuidanceStatusChip({ label, tone = 'neutral' }: GuidanceStatusChipProps) {
  const colors = useToneColors(tone);
  return (
    <Chip
      compact
      style={{ backgroundColor: colors.bg }}
      textStyle={{ color: colors.text, fontSize: 12 }}
    >
      {label}
    </Chip>
  );
}

// ── GuidanceLoadingState ─────────────────────────────────────────────────────

export function GuidanceLoadingState({ message }: { message?: string }) {
  const theme = useTheme();
  return (
    <View style={styles.loadingState}>
      <ActivityIndicator size="small" color={theme.colors.primary} />
      {message ? (
        <Text variant="bodyMedium" style={{ color: theme.colors.onSurfaceVariant, marginTop: 8, textAlign: 'center' }}>
          {message}
        </Text>
      ) : null}
    </View>
  );
}

// ── GuidanceStatePanel ───────────────────────────────────────────────────────

interface GuidanceStatePanelProps {
  title: string;
  message: string;
  actionLabel?: string;
  onAction?: () => void;
}

export function GuidanceStatePanel({ title, message, actionLabel, onAction }: GuidanceStatePanelProps) {
  const theme = useTheme();
  return (
    <View style={styles.statePanel}>
      <Text variant="titleMedium" style={{ color: theme.colors.onSurface, fontWeight: '600', textAlign: 'center' }}>
        {title}
      </Text>
      <Text variant="bodyMedium" style={{ color: theme.colors.onSurfaceVariant, marginTop: 8, textAlign: 'center' }}>
        {message}
      </Text>
      {actionLabel && onAction ? (
        <Button mode="contained-tonal" onPress={onAction} style={{ marginTop: 16, borderRadius: 24 }}>
          {actionLabel}
        </Button>
      ) : null}
    </View>
  );
}

// ── Styles ───────────────────────────────────────────────────────────────────

const styles = StyleSheet.create({
  card: { borderRadius: 16, marginBottom: 0 },
  cardContent: { padding: 16 },
  cardHeader: { flexDirection: 'row', alignItems: 'flex-start', justifyContent: 'space-between' },
  cardHeaderText: { flex: 1, marginRight: 8 },
  cardBody: { marginTop: 12 },
  sectionHeader: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  banner: { borderRadius: 8, borderLeftWidth: 4, padding: 12, marginVertical: 4 },
  loadingState: { alignItems: 'center', paddingVertical: 24 },
  statePanel: { flex: 1, alignItems: 'center', justifyContent: 'center', paddingHorizontal: 32 },
});


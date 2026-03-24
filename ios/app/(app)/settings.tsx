import { useCallback, useEffect, useState } from 'react';
import { ScrollView, StyleSheet, TouchableOpacity, View } from 'react-native';
import { Text, Switch, Divider, useTheme } from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Linking } from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { mobileApi } from '../../src/api/mobile';
import { userApi } from '../../src/api/user';
import { useAuth } from '../../src/context/AuthContext';
import type { NotificationPreferencesResponse, UserDto } from '../../src/types/api';
import {
  GuidanceSectionCard,
  GuidanceBanner,
} from '../../src/components/GuidanceComponents';

const HELP_DESK_URL = 'https://tsin.atlassian.net/servicedesk/customer/portal/8';
const PRIVACY_URL = 'https://touchstoneinstitute.ca/wp-content/uploads/Pre-Residency-Program-Canadian-Medicine-Primer-Privacy-Policy.pdf';
const REGISTRATION_URL = 'https://d1wnwyag9usc7m.cloudfront.net/PRP%20Registration%20Policy.pdf';

export default function SettingsScreen() {
  const theme = useTheme();
  const { logout } = useAuth();

  const [user, setUser] = useState<UserDto | null>(null);
  const [prefs, setPrefs] = useState<NotificationPreferencesResponse>({
    pushEnabled: false,
    scheduleReminders: false,
    orientationReminders: false,
  });
  const [prefsLoading, setPrefsLoading] = useState(true);
  const [prefsSaving, setPrefsSaving] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isLoadError, setIsLoadError] = useState(false);

  const loadData = useCallback(async () => {
    try {
      const [u, p] = await Promise.all([userApi.getMe(), mobileApi.getPreferences()]);
      setUser(u);
      setPrefs(p);
      setIsLoadError(false);
    } catch (e: any) {
      setErrorMessage(e?.response?.data?.message ?? 'Unable to load your preferences.');
      setIsLoadError(true);
    } finally {
      setPrefsLoading(false);
    }
  }, []);

  useEffect(() => { loadData(); }, []);

  async function updatePref(patch: Partial<NotificationPreferencesResponse>) {
    const updated = { ...prefs, ...patch };
    setPrefs(updated);
    setPrefsSaving(true);
    setSuccessMessage(null);
    setErrorMessage(null);
    try {
      await mobileApi.updatePreferences(patch);
      setSuccessMessage('Your alert preferences have been saved.');
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (e: any) {
      setErrorMessage(e?.response?.data?.message ?? 'Could not save preferences. Try again.');
      setPrefs(prefs); // rollback
    } finally {
      setPrefsSaving(false);
    }
  }

  const displayName = user
    ? [user.firstName, user.lastName].filter(Boolean).join(' ') || 'Learner'
    : 'Learner';

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]}>
      <ScrollView contentContainerStyle={styles.scroll}>

        {/* Account */}
        <GuidanceSectionCard title="Account" supportingText={displayName}>
          {user?.email && (
            <Text variant="bodyMedium" style={{ color: theme.colors.onSurface }}>{user.email}</Text>
          )}
          {user?.role && (
            <Text variant="bodyMedium" style={{ color: theme.colors.onSurfaceVariant, marginTop: 4 }}>
              Role: {user.role}
            </Text>
          )}
        </GuidanceSectionCard>

        {/* Alerts */}
        <GuidanceSectionCard title="Alerts" supportingText="Choose how the app keeps you informed.">
          {prefsSaving && (
            <GuidanceBanner title="Saving changes" message="We're updating your alert preferences now." tone="neutral" />
          )}
          {successMessage && (
            <GuidanceBanner title="Saved" message={successMessage} tone="success" />
          )}
          {errorMessage && (
            <GuidanceBanner
              title={isLoadError ? "Couldn't load preferences" : "Couldn't save changes"}
              message={errorMessage}
              tone="critical"
            />
          )}
          {prefsLoading && (
            <Text variant="bodyMedium" style={{ color: theme.colors.onSurfaceVariant }}>
              Syncing your alert settings…
            </Text>
          )}
          {!prefs.pushEnabled && !prefsLoading && (
            <GuidanceBanner
              title="Push notifications are off"
              message="Turn them on to receive schedule and orientation reminders on this device."
              tone="neutral"
            />
          )}
          <ToggleRow
            title="Push notifications"
            subtitle="Receive mobile alerts on this device."
            checked={prefs.pushEnabled}
            onToggle={(v) => updatePref({ pushEnabled: v })}
          />
          <Divider />
          <ToggleRow
            title="Schedule reminders"
            subtitle="Get a heads-up before upcoming sessions."
            checked={prefs.scheduleReminders}
            enabled={prefs.pushEnabled}
            onToggle={(v) => updatePref({ scheduleReminders: v })}
          />
          <Divider />
          <ToggleRow
            title="Orientation reminders"
            subtitle="Stay on top of orientation events and updates."
            checked={prefs.orientationReminders}
            enabled={prefs.pushEnabled}
            onToggle={(v) => updatePref({ orientationReminders: v })}
          />
        </GuidanceSectionCard>

        {/* Support */}
        <GuidanceSectionCard title="Support and policies" supportingText="Quick links for help, privacy, and program terms.">
          <LinkRow title="Help" subtitle="Open the TSIN helpdesk for support requests." icon="lifebuoy" onPress={() => Linking.openURL(HELP_DESK_URL)} />
          <Divider />
          <LinkRow title="Privacy policy" subtitle="Read how Touchstone Institute handles your information." icon="shield-account-outline" onPress={() => Linking.openURL(PRIVACY_URL)} />
          <Divider />
          <LinkRow title="Terms and registration policy" subtitle="Review current program registration expectations and terms." icon="file-document-outline" onPress={() => Linking.openURL(REGISTRATION_URL)} />
          <Divider />
          <View style={[styles.staticRow, { minHeight: 56 }]}>
            <Text variant="bodyLarge" style={{ color: theme.colors.onSurface, fontWeight: '500' }}>App version</Text>
            <Text variant="bodySmall" style={{ color: theme.colors.onSurfaceVariant, marginTop: 2 }}>Version 1.0.0</Text>
          </View>
        </GuidanceSectionCard>

        {/* Danger zone */}
        <GuidanceSectionCard title="Danger zone" supportingText="Use this when you need to leave the app securely.">
          <TouchableOpacity style={[styles.linkRow, { minHeight: 56 }]} onPress={logout}>
            <MaterialCommunityIcons name="logout" size={20} color={theme.colors.error} />
            <View style={{ flex: 1, marginLeft: 12 }}>
              <Text variant="bodyLarge" style={{ color: theme.colors.error, fontWeight: '500' }}>Sign out</Text>
              <Text variant="bodySmall" style={{ color: theme.colors.onSurfaceVariant, marginTop: 2 }}>
                Remove your current session from this device.
              </Text>
            </View>
          </TouchableOpacity>
        </GuidanceSectionCard>

      </ScrollView>
    </SafeAreaView>
  );
}

function ToggleRow({
  title, subtitle, checked, enabled = true, onToggle,
}: {
  title: string; subtitle: string; checked: boolean; enabled?: boolean; onToggle: (v: boolean) => void;
}) {
  const theme = useTheme();
  return (
    <TouchableOpacity
      style={[styles.linkRow, { minHeight: 56, opacity: enabled ? 1 : 0.5 }]}
      onPress={() => enabled && onToggle(!checked)}
      disabled={!enabled}
    >
      <View style={{ flex: 1 }}>
        <Text variant="bodyLarge" style={{ color: theme.colors.onSurface, fontWeight: '500' }}>{title}</Text>
        <Text variant="bodySmall" style={{ color: theme.colors.onSurfaceVariant, marginTop: 2 }}>{subtitle}</Text>
      </View>
      <Switch value={checked} onValueChange={onToggle} disabled={!enabled} />
    </TouchableOpacity>
  );
}

function LinkRow({
  title, subtitle, icon, onPress,
}: {
  title: string; subtitle: string; icon: string; onPress: () => void;
}) {
  const theme = useTheme();
  return (
    <TouchableOpacity style={[styles.linkRow, { minHeight: 56 }]} onPress={onPress}>
      <MaterialCommunityIcons name={icon} size={20} color={theme.colors.primary} />
      <View style={{ flex: 1, marginLeft: 12 }}>
        <Text variant="bodyLarge" style={{ color: theme.colors.onSurface, fontWeight: '500' }}>{title}</Text>
        <Text variant="bodySmall" style={{ color: theme.colors.onSurfaceVariant, marginTop: 2 }}>{subtitle}</Text>
      </View>
      <MaterialCommunityIcons name="open-in-new" size={18} color={theme.colors.onSurfaceVariant} />
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  scroll: { padding: 16, gap: 12 },
  linkRow: { flexDirection: 'row', alignItems: 'center', paddingVertical: 8 },
  staticRow: { paddingVertical: 8 },
});


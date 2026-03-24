import { useState } from 'react';
import { View, StyleSheet, KeyboardAvoidingView, Platform, ScrollView } from 'react-native';
import { Text, TextInput, Button, useTheme, HelperText } from 'react-native-paper';
import { useRouter } from 'expo-router';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAuth } from '../../src/context/AuthContext';
import { Colors } from '../../src/theme';

export default function LoginScreen() {
  const { login, isLoading, errorMessage, clearError, pendingMfaEmail, mfaSetupRequired, dismissMfaSetup } = useAuth();
  const router = useRouter();
  const theme = useTheme();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [passwordVisible, setPasswordVisible] = useState(false);

  async function handleLogin() {
    const result = await login(email.trim(), password);
    if (result?.user?.isOtpEnabled && !result?.skipValidation) {
      router.push({ pathname: '/(auth)/mfa', params: { email: email.trim(), method: result.user.otpMeans ?? 'app' } });
    }
  }

  // MFA setup required screen
  if (mfaSetupRequired) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.surface }]}>
        <View style={styles.center}>
          <Text variant="headlineSmall" style={{ color: theme.colors.onSurface, fontWeight: '600', textAlign: 'center' }}>
            MFA Setup Required
          </Text>
          <Text variant="bodyMedium" style={[styles.setupText, { color: theme.colors.onSurfaceVariant }]}>
            This account needs multi-factor authentication set up before using the mobile app.{'\n\n'}
            Please sign in to the TSIN Learning Portal in a browser to complete MFA setup, then return here.
          </Text>
          <Button mode="outlined" onPress={dismissMfaSetup} style={styles.button}>
            Back to Sign In
          </Button>
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]}>
      <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : 'height'} style={{ flex: 1 }}>
        <ScrollView contentContainerStyle={styles.scroll} keyboardShouldPersistTaps="handled">
          {/* Brand header */}
          <View style={styles.brandHeader}>
            <View style={[styles.brandBadge, { backgroundColor: Colors.TsinNavy }]}>
              <Text variant="headlineMedium" style={{ color: Colors.TsinLime, fontWeight: '700' }}>T</Text>
            </View>
            <Text variant="headlineMedium" style={{ color: Colors.TsinNavy, fontWeight: '700', marginTop: 12 }}>
              TSIN Learning
            </Text>
            <View style={[styles.accent, { backgroundColor: Colors.TsinLime }]} />
            <Text variant="bodyMedium" style={{ color: theme.colors.onSurfaceVariant, marginTop: 8 }}>
              Sign in to your account
            </Text>
          </View>

          {/* Form */}
          <View style={styles.form}>
            <TextInput
              label="Email"
              value={email}
              onChangeText={(v) => { setEmail(v); clearError(); }}
              mode="outlined"
              keyboardType="email-address"
              autoCapitalize="none"
              autoComplete="email"
              returnKeyType="next"
              style={styles.input}
            />

            <TextInput
              label="Password"
              value={password}
              onChangeText={(v) => { setPassword(v); clearError(); }}
              mode="outlined"
              secureTextEntry={!passwordVisible}
              returnKeyType="done"
              onSubmitEditing={handleLogin}
              right={
                <TextInput.Icon
                  icon={passwordVisible ? 'eye-off' : 'eye'}
                  onPress={() => setPasswordVisible(!passwordVisible)}
                />
              }
              style={styles.input}
            />

            {errorMessage ? (
              <HelperText type="error" visible>
                {errorMessage}
              </HelperText>
            ) : null}

            <Button
              mode="contained"
              onPress={handleLogin}
              loading={isLoading}
              disabled={!email.trim() || !password || isLoading}
              style={[styles.button, styles.signInButton]}
              contentStyle={styles.buttonContent}
            >
              Sign in
            </Button>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  scroll: { flexGrow: 1, justifyContent: 'center', paddingHorizontal: 32, paddingVertical: 40 },
  center: { flex: 1, justifyContent: 'center', alignItems: 'center', paddingHorizontal: 32 },
  brandHeader: { alignItems: 'center', marginBottom: 40 },
  brandBadge: { width: 72, height: 72, borderRadius: 36, justifyContent: 'center', alignItems: 'center' },
  accent: { height: 4, width: 80, borderRadius: 2, marginTop: 8 },
  form: { gap: 8 },
  input: { backgroundColor: 'transparent' },
  button: { borderRadius: 24, marginTop: 8 },
  buttonContent: { height: 48 },
  signInButton: {},
  setupText: { textAlign: 'center', marginTop: 16, marginBottom: 32, lineHeight: 22 },
});


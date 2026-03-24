import { useState } from 'react';
import { View, StyleSheet, KeyboardAvoidingView, Platform } from 'react-native';
import { Text, TextInput, Button, HelperText, useTheme } from 'react-native-paper';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAuth } from '../../src/context/AuthContext';

export default function MfaScreen() {
  const { email, method } = useLocalSearchParams<{ email: string; method: string }>();
  const { completeMfa, isLoading, errorMessage, clearError } = useAuth();
  const router = useRouter();
  const theme = useTheme();

  const [code, setCode] = useState('');
  const isEmailMethod = method === 'email';

  async function handleVerify() {
    if (!email) return;
    await completeMfa(email, code.trim(), isEmailMethod ? 'email' : 'app');
  }

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]}>
      <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : 'height'} style={{ flex: 1 }}>
        <View style={styles.content}>
          {/* Back button */}
          <Button
            icon="arrow-left"
            mode="text"
            onPress={() => router.back()}
            style={styles.backButton}
            labelStyle={{ color: theme.colors.onSurfaceVariant }}
          >
            Back
          </Button>

          <View style={styles.body}>
            <Text variant="headlineSmall" style={{ color: theme.colors.onSurface, fontWeight: '700' }}>
              Verify your identity
            </Text>
            <Text variant="bodyMedium" style={[styles.subtitle, { color: theme.colors.onSurfaceVariant }]}>
              {isEmailMethod
                ? `Enter the 6-digit code sent to ${email}.`
                : 'Enter the 6-digit code from your authenticator app.'}
            </Text>

            <TextInput
              label={isEmailMethod ? 'Email verification code' : 'Authenticator code'}
              value={code}
              onChangeText={(v) => { setCode(v); clearError(); }}
              mode="outlined"
              keyboardType="number-pad"
              maxLength={6}
              autoFocus
              returnKeyType="done"
              onSubmitEditing={handleVerify}
              style={styles.input}
            />

            {errorMessage ? (
              <HelperText type="error" visible>
                {errorMessage}
              </HelperText>
            ) : null}

            <Button
              mode="contained"
              onPress={handleVerify}
              loading={isLoading}
              disabled={code.length < 6 || isLoading}
              style={styles.verifyButton}
              contentStyle={styles.buttonContent}
            >
              Verify
            </Button>

            <Text variant="bodySmall" style={[styles.hint, { color: theme.colors.onSurfaceVariant }]}>
              {isEmailMethod
                ? 'Check your inbox and spam folder. The code expires in a few minutes.'
                : 'Open your authenticator app to find the current 6-digit code.'}
            </Text>
          </View>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: { flex: 1, paddingHorizontal: 24 },
  backButton: { alignSelf: 'flex-start', marginTop: 8 },
  body: { flex: 1, justifyContent: 'center', gap: 16 },
  subtitle: { lineHeight: 22, marginBottom: 8 },
  input: { backgroundColor: 'transparent' },
  verifyButton: { borderRadius: 24, marginTop: 8 },
  buttonContent: { height: 48 },
  hint: { textAlign: 'center', lineHeight: 20, marginTop: 8 },
});


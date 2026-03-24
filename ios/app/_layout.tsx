import { useEffect } from 'react';
import { useColorScheme } from 'react-native';
import { Stack, useRouter, useSegments } from 'expo-router';
import { PaperProvider } from 'react-native-paper';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { StatusBar } from 'expo-status-bar';
import { AuthProvider, useAuth } from '../src/context/AuthContext';
import { tsinTheme, tsinDarkTheme } from '../src/theme';
import { GuidanceLoadingState } from '../src/components/GuidanceComponents';
import { View } from 'react-native';

// ── Auth gate: handles redirect between (auth) and (app) ─────────────────────

function AuthGate() {
  const { isLoggedIn, isCheckingSession } = useAuth();
  const segments = useSegments();
  const router = useRouter();

  useEffect(() => {
    if (isCheckingSession) return;

    const inAuthGroup = segments[0] === '(auth)';

    if (!isLoggedIn && !inAuthGroup) {
      router.replace('/(auth)/login');
    } else if (isLoggedIn && inAuthGroup) {
      router.replace('/(app)');
    }
  }, [isLoggedIn, isCheckingSession, segments]);

  if (isCheckingSession) {
    return (
      <View style={{ flex: 1, backgroundColor: '#002D74', justifyContent: 'center', alignItems: 'center' }}>
        <GuidanceLoadingState message="Checking your session..." />
      </View>
    );
  }

  return (
    <Stack screenOptions={{ headerShown: false }}>
      <Stack.Screen name="(auth)" />
      <Stack.Screen name="(app)" />
    </Stack>
  );
}

// ── Root layout ───────────────────────────────────────────────────────────────

export default function RootLayout() {
  const colorScheme = useColorScheme();
  const theme = colorScheme === 'dark' ? tsinDarkTheme : tsinTheme;

  return (
    <AuthProvider>
      <PaperProvider theme={theme}>
        <SafeAreaProvider>
          <StatusBar style={colorScheme === 'dark' ? 'light' : 'dark'} />
          <AuthGate />
        </SafeAreaProvider>
      </PaperProvider>
    </AuthProvider>
  );
}


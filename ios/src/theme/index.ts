import { MD3LightTheme, MD3DarkTheme } from 'react-native-paper';

// Brand colours mirrored from Color.kt
export const Colors = {
  TsinNavy: '#002D74',
  TsinLime: '#B5BE00',
  TsinNavyLight: '#1A4A8A',
  TsinNavyDark: '#001B4D',
  // Surfaces
  ConsoleSurface: '#FFFFFF',
  ConsoleSurfaceVariant: '#F6F8FC',
  ConsoleSurfaceContainer: '#F0F4FA',
  ConsoleBackground: '#FAFBFF',
  // Text
  ConsoleOnSurface: '#18202D',
  ConsoleOnSurfaceVariant: '#5A6472',
  ConsoleOnSurfaceDisabled: '#96A0AF',
  // Primary
  TsinPrimaryContainer: '#D6E3FF',
  TsinOnPrimaryContainer: '#001B3E',
  // Secondary
  TsinSecondary: '#565F71',
  TsinSecondaryContainer: '#DAE2F9',
  TsinOnSecondaryContainer: '#131C2B',
  // Interactive
  TsinTertiary: '#1A73E8',
  TsinTertiaryContainer: '#D3E3FD',
  // Error
  TsinError: '#D93025',
  TsinErrorContainer: '#FCE8E6',
  TsinOnErrorContainer: '#410E0B',
  // Outline
  ConsoleOutline: '#D7DEE8',
  ConsoleOutlineVariant: '#BEC7D3',
  // Dark
  ConsoleSurfaceDark: '#171C26',
  ConsoleSurfaceVariantDark: '#232A36',
  ConsoleOnSurfaceDark: '#F0F3F9',
  ConsoleOnSurfaceVariantDark: '#B6C0CE',
  ConsoleBackgroundDark: '#10151E',
  TsinPrimaryDark: '#ABC7FF',
};

export const tsinTheme = {
  ...MD3LightTheme,
  colors: {
    ...MD3LightTheme.colors,
    primary: Colors.TsinNavy,
    onPrimary: '#FFFFFF',
    primaryContainer: Colors.TsinPrimaryContainer,
    onPrimaryContainer: Colors.TsinOnPrimaryContainer,
    secondary: Colors.TsinSecondary,
    onSecondary: '#FFFFFF',
    secondaryContainer: Colors.TsinSecondaryContainer,
    onSecondaryContainer: Colors.TsinOnSecondaryContainer,
    tertiary: Colors.TsinTertiary,
    onTertiary: '#FFFFFF',
    tertiaryContainer: Colors.TsinTertiaryContainer,
    onTertiaryContainer: '#062E6F',
    error: Colors.TsinError,
    onError: '#FFFFFF',
    errorContainer: Colors.TsinErrorContainer,
    onErrorContainer: Colors.TsinOnErrorContainer,
    background: Colors.ConsoleBackground,
    onBackground: Colors.ConsoleOnSurface,
    surface: Colors.ConsoleSurface,
    onSurface: Colors.ConsoleOnSurface,
    surfaceVariant: Colors.ConsoleSurfaceVariant,
    onSurfaceVariant: Colors.ConsoleOnSurfaceVariant,
    outline: Colors.ConsoleOutline,
    outlineVariant: Colors.ConsoleOutlineVariant,
    surfaceContainerLow: Colors.ConsoleSurfaceContainer,
    surfaceContainer: Colors.ConsoleSurfaceContainer,
    surfaceContainerHigh: '#E6ECF6',
  },
};

export const tsinDarkTheme = {
  ...MD3DarkTheme,
  colors: {
    ...MD3DarkTheme.colors,
    primary: Colors.TsinPrimaryDark,
    onPrimary: Colors.TsinNavyDark,
    primaryContainer: '#00458E',
    onPrimaryContainer: Colors.TsinPrimaryContainer,
    background: Colors.ConsoleBackgroundDark,
    onBackground: Colors.ConsoleOnSurfaceDark,
    surface: Colors.ConsoleSurfaceDark,
    onSurface: Colors.ConsoleOnSurfaceDark,
    surfaceVariant: Colors.ConsoleSurfaceVariantDark,
    onSurfaceVariant: Colors.ConsoleOnSurfaceVariantDark,
    outline: '#3A4250',
    outlineVariant: '#2A3240',
    surfaceContainerLow: '#1E2430',
    surfaceContainer: '#1E2430',
    surfaceContainerHigh: '#252C3A',
    error: '#FF6B6B',
    onError: '#690005',
  },
};


import FormTextField from "@/components/FormTextField";
import Text from "@/components/Text";
import StrengthIndicator from "@/features/password-generator/StrengthIndicator.component";
import {
  type CharacterType,
  generatePassword,
  getPasswordStrength,
  type PasswordGeneratorOptions,
} from "@passcodes/passalgo/passwords";
import Slider from "@react-native-community/slider";
import FontAwesome6 from "@react-native-vector-icons/fontawesome6";
import * as Clipboard from "expo-clipboard";
import { useEffect, useState } from "react";
import { ScrollView, StyleSheet, View } from "react-native";
import { Button, Card, Divider, useTheme } from "react-native-paper";
import GeneratorOption from "./GeneratorOption.component";
import {
  DEFAULT_PASSWORD_OPTIONS,
  MAX_PASSWORD_LENGTH,
  MIN_PASSWORD_LENGTH,
  quickCryptoRandom,
} from "./utils";

const CHARACTER_OPTIONS: {
  key: CharacterType;
  label: string;
  example: string;
}[] = [
  {
    key: "uppercase",
    label: "Uppercase",
    example: "ABC",
  },
  {
    key: "lowercase",
    label: "Lowercase",
    example: "abc",
  },
  {
    key: "numbers",
    label: "Numbers",
    example: "123",
  },
  {
    key: "symbols",
    label: "Symbols",
    example: "!@#",
  },
];

export default function PasswordGeneratorScreenContent() {
  const theme = useTheme();

  const [options, setOptions] = useState<PasswordGeneratorOptions>(
    DEFAULT_PASSWORD_OPTIONS,
  );

  const [password, setPassword] = useState(() =>
    generatePassword(options, quickCryptoRandom),
  );

  const strength = getPasswordStrength(password);

  /*
   * Regenerate the password whenever any generator
   * option changes.
   */
  useEffect(() => {
    setPassword(generatePassword(options, quickCryptoRandom));
  }, [options]);

  function updateOption(key: CharacterType) {
    setOptions((current) => {
      const enabledCharacterTypes = CHARACTER_OPTIONS.filter(
        (option) => current[option.key],
      );

      /*
       * Keep at least one character type enabled.
       */
      if (current[key] && enabledCharacterTypes.length === 1) {
        return current;
      }

      return {
        ...current,
        [key]: !current[key],
      };
    });
  }

  function updateLength(length: number) {
    setOptions((current) => ({
      ...current,
      length: Math.round(length),
    }));
  }

  function generate() {
    setPassword(generatePassword(options, quickCryptoRandom));
  }

  async function copyPassword() {
    if (!password) {
      return;
    }

    await Clipboard.setStringAsync(password);
  }

  return (
    <ScrollView
      contentContainerStyle={styles.content}
      showsVerticalScrollIndicator
    >
      <Text variant="headlineMedium" style={styles.title}>
        Password Generator
      </Text>

      {/* Generated Password */}

      <Card>
        <Card.Content style={styles.cardContent}>
          <Text variant="bodySmall" style={styles.sectionLabel}>
            Generated Password
          </Text>

          <FormTextField
            value={password}
            editable={false}
            style={{ fontSize: 16 }}
          />

          <View style={styles.actionContainer}>
            <Button
              mode="contained"
              icon={({ size, color }) => (
                <FontAwesome6
                  name="rotate"
                  size={size}
                  color={color}
                  iconStyle="solid"
                />
              )}
              onPress={generate}
            >
              Generate
            </Button>

            <Button
              mode="outlined"
              icon={({ size, color }) => (
                <FontAwesome6
                  name="clone"
                  size={size}
                  color={color}
                  iconStyle="solid"
                />
              )}
              onPress={copyPassword}
              disabled={!password}
            >
              Copy
            </Button>
          </View>
        </Card.Content>
      </Card>

      {/* Password Length */}

      <Card>
        <Card.Content style={styles.cardContent}>
          <View style={styles.sectionHeader}>
            <View style={styles.headerText}>
              <Text variant="bodyMedium">Length</Text>

              <Text variant="bodySmall" style={styles.description}>
                Choose the length of your password
              </Text>
            </View>

            <Text variant="titleMedium" style={styles.lengthValue}>
              {options.length}
            </Text>
          </View>

          <Slider
            minimumValue={MIN_PASSWORD_LENGTH}
            maximumValue={MAX_PASSWORD_LENGTH}
            step={1}
            value={options.length}
            onValueChange={updateLength}
          />

          <View style={styles.rangeLabels}>
            <Text variant="labelSmall">{MIN_PASSWORD_LENGTH}</Text>

            <Text variant="labelSmall">{MAX_PASSWORD_LENGTH}</Text>
          </View>
        </Card.Content>
      </Card>

      {/* Character Types */}

      <Card>
        <Card.Content style={styles.cardContent}>
          <View>
            <Text variant="bodyMedium">Character Types</Text>

            <Text variant="bodySmall" style={styles.description}>
              Choose which characters can be used
            </Text>
          </View>

          <View style={styles.optionList}>
            {CHARACTER_OPTIONS.map((option, index) => (
              <View key={option.key}>
                <GeneratorOption
                  label={option.label}
                  example={option.example}
                  enabled={options[option.key]}
                  onPress={() => updateOption(option.key)}
                />

                {index < CHARACTER_OPTIONS.length - 1 && <Divider />}
              </View>
            ))}
          </View>
        </Card.Content>
      </Card>

      {/* Password Strength */}

      <Card>
        <Card.Content style={styles.cardContent}>
          <View style={styles.sectionHeader}>
            <View style={styles.headerText}>
              <Text variant="bodyMedium">Password Strength</Text>

              <Text variant="bodySmall" style={styles.description}>
                Based on length and character variety
              </Text>
            </View>

            <Text
              variant="labelLarge"
              style={{
                color:
                  strength.label === "Strong"
                    ? theme.colors.primary
                    : theme.colors.onSurface,
              }}
            >
              {strength.label}
            </Text>
          </View>

          <StrengthIndicator score={strength.score} />
        </Card.Content>
      </Card>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  content: {
    paddingHorizontal: 12,
    paddingBottom: 96,
    gap: 24,
  },

  title: {
    marginTop: 8,
    marginBottom: 4,
    marginHorizontal: 4,
    textAlign: "center",
  },

  cardContent: {
    padding: 16,
    gap: 16,
  },

  sectionLabel: {
    opacity: 0.7,
  },

  actionContainer: {
    flexDirection: "row-reverse",
    gap: 8,
  },

  sectionHeader: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    gap: 16,
  },

  headerText: {
    flex: 1,
  },

  description: {
    opacity: 0.6,
    marginTop: 3,
  },

  lengthValue: {
    minWidth: 40,
    textAlign: "center",
  },

  rangeLabels: {
    flexDirection: "row",
    justifyContent: "space-between",
    opacity: 0.6,
  },

  optionList: {
    marginHorizontal: -8,
  },
});

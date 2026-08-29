export const MIN_PASSWORD_LENGTH = 8;
export const MAX_PASSWORD_LENGTH = 64;

const UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
const LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
const NUMBERS = "0123456789";
const SYMBOLS = "!@#$%^&*()-_=+[]{};:,.<>?";

export type PasswordGeneratorOptions = {
  length: number;
  uppercase: boolean;
  lowercase: boolean;
  numbers: boolean;
  symbols: boolean;
};

export type PasswordStrength = {
  score: number;
  label: "Weak" | "Medium" | "Strong";
};

export type CharacterType = keyof Omit<PasswordGeneratorOptions, "length">;

function randomCharacter(characters: string): string {
  return characters[Math.floor(Math.random() * characters.length)];
}

export function generatePassword(options: PasswordGeneratorOptions): string {
  const characterSets: string[] = [];

  if (options.uppercase) {
    characterSets.push(UPPERCASE);
  }

  if (options.lowercase) {
    characterSets.push(LOWERCASE);
  }

  if (options.numbers) {
    characterSets.push(NUMBERS);
  }

  if (options.symbols) {
    characterSets.push(SYMBOLS);
  }

  if (characterSets.length === 0) {
    return "";
  }

  const allCharacters = characterSets.join("");

  const characters: string[] = [];

  // Guarantee that every selected character type
  // appears at least once.
  for (const characterSet of characterSets) {
    characters.push(randomCharacter(characterSet));
  }

  while (characters.length < options.length) {
    characters.push(randomCharacter(allCharacters));
  }

  // Fisher-Yates shuffle.
  for (let i = characters.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));

    [characters[i], characters[j]] = [characters[j], characters[i]];
  }

  return characters.join("");
}

export function getPasswordStrength(
  options: PasswordGeneratorOptions,
): PasswordStrength {
  let score = 0;

  if (options.length >= 12) {
    score++;
  }

  if (options.length >= 20) {
    score++;
  }

  if (options.uppercase) {
    score++;
  }

  if (options.lowercase) {
    score++;
  }

  if (options.numbers) {
    score++;
  }

  if (options.symbols) {
    score++;
  }

  if (score <= 2) {
    return {
      score,
      label: "Weak",
    };
  }

  if (score <= 4) {
    return {
      score,
      label: "Medium",
    };
  }

  return {
    score,
    label: "Strong",
  };
}

export const DEFAULT_PASSWORD_OPTIONS: PasswordGeneratorOptions = {
  length: 20,
  uppercase: true,
  lowercase: true,
  numbers: true,
  symbols: true,
};

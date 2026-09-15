import type {
  PasswordGeneratorOptions,
  RandomSource,
} from "@passcodes/passalgo/passwords";
import { randomInt } from "react-native-quick-crypto";

export const quickCryptoRandom: RandomSource = {
  randomInt(max) {
    return randomInt(0, max);
  },
};

export const DEFAULT_PASSWORD_OPTIONS: PasswordGeneratorOptions = {
  length: 16,
  uppercase: true,
  lowercase: true,
  numbers: false,
  symbols: false,
};

export const MIN_PASSWORD_LENGTH = 4;
export const MAX_PASSWORD_LENGTH = 64;

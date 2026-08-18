interface PasswordOptions {
  length: number;
  letters?: boolean; // A-Z + a-z (one option)
  numbers?: boolean; // 0-9
  symbols?: boolean; // !@#$%^&* etc.
}

export function generatePassword(options: PasswordOptions): string {
  // Default: everything enabled
  const { length, letters = true, numbers = true, symbols = true } = options;

  const upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  const lower = "abcdefghijklmnopqrstuvwxyz";
  const nums = "0123456789";
  const syms = "!@#$%^&*()_+-=[]{}|;:,.<>?";

  let allChars = "";
  const guaranteed: string[] = [];

  if (letters) {
    allChars += upper + lower;
    // Guarantee at least one upper and one lower
    guaranteed.push(upper[secureRandom(upper.length)]);
    guaranteed.push(lower[secureRandom(lower.length)]);
  }

  // Numbers
  if (numbers) {
    allChars += nums;
    guaranteed.push(nums[secureRandom(nums.length)]);
  }

  // Symbols
  if (symbols) {
    allChars += syms;
    guaranteed.push(syms[secureRandom(syms.length)]);
  }

  // Validation
  if (!allChars) {
    throw new Error("Please select at least one character type.");
  }

  if (guaranteed.length > length) {
    throw new Error("Too many required character types for the fixed length.");
  }

  // Fill remaining characters
  const passwordChars = [...guaranteed];
  while (passwordChars.length < length) {
    passwordChars.push(allChars[secureRandom(allChars.length)]);
  }

  // Shuffle so guaranteed characters are not always at the start
  for (let i = passwordChars.length - 1; i > 0; i--) {
    const j = secureRandom(i + 1);
    [passwordChars[i], passwordChars[j]] = [passwordChars[j], passwordChars[i]];
  }

  return passwordChars.join("");
}

// Cryptographically secure random number
function secureRandom(max: number): number {
  const array = new Uint32Array(1);
  crypto.getRandomValues(array);
  return array[0] % max;
}

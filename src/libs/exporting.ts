import { File, Paths } from "expo-file-system";
import * as Sharing from "expo-sharing";

export type PasswordCSVFormat = {
  domain: string;
  url: string | null;
  username: string;
  password: string;
  notes: string | null;
};

export async function getGooglePasswordsCSVContent(
  passwords: PasswordCSVFormat[],
) {
  let result = ["name,url,username,password,note"];

  passwords.forEach((password) => {
    let parseURL = password.url || `https://local.${password.domain}`;

    let CSVRow = `${password.domain},${parseURL},${password.username},${password.password},${password.notes || ""}`;
    result.push(CSVRow);
  });

  return result.join("\n");
}

export async function sharePasswordAsCSV(csvContent: string) {
  const file = new File(Paths.cache, `passcodes-export-${Date.now()}.csv`);

  file.write(csvContent);

  try {
    await Sharing.shareAsync(file.uri, {
      mimeType: "text/csv",
      dialogTitle: "Export Passwords",
    });
  } finally {
    file.delete();
  }
}

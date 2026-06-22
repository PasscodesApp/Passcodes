import { File } from "expo-file-system";
import { type PasswordCSVFormat } from "./exporting";

export async function getCSVPasswordString(): Promise<string> {
  let filePickResult = await File.pickFileAsync({
    mimeTypes: [
      "text/csv",
      "text/comma-separated-values",
      "application/csv",
      "text/plain",
    ],
  });

  if (filePickResult.canceled) {
    return "name,url,username,password,note";
  }

  let passwordFile = filePickResult.result;
  let passwordCSVContent = await passwordFile.text();
  return passwordCSVContent;
}

export function convertRawCSVToPasswords(
  CSVContent: string,
): PasswordCSVFormat[] {
  const lines = CSVContent.trim().split("\n").filter(Boolean);

  // remove header
  lines.shift();

  return lines.map((line) => {
    const [domain, url, username, password, notes = ""] = line.split(",");

    return {
      domain: domain,
      url: url,
      username: username,
      password: password,
      notes: notes,
    };
  });
}

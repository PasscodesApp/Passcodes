import FormTextField from "@/components/FormTextField";
import SecureTextField from "@/components/SecureTextField";
import { useState } from "react";

import { View } from "react-native";

type PasswordFormValue = {
  domain: string;
  username: string;
  password: string;
  url: string;
  notes: string;
  updatedAt: string;
};

// xxxxx Component xxxxx

type PasswordFormCardProps = {
  value: PasswordFormValue;
  editable: boolean;

  onChange: <K extends keyof PasswordFormValue>(
    field: K,
    value: PasswordFormValue[K],
  ) => void;
};

export default function PasswordFormCard({
  value,
  editable,
  onChange,
}: PasswordFormCardProps) {
  return (
    <View style={{ gap: 24 }}>
      <FormTextField
        label="Domain"
        value={value.domain}
        onChangeText={(text) => onChange("domain", text)}
        editable={editable}
      />

      <FormTextField
        label="Username"
        value={value.username}
        onChangeText={(text) => onChange("username", text)}
        editable={editable}
        autoCapitalize="none"
        autoCorrect={false}
      />

      <SecureTextField
        label="Password"
        value={value.password}
        onChangeText={(text) => onChange("password", text)}
        editable={editable}
      />

      <FormTextField
        label="URL"
        value={value.url}
        onChangeText={(text) => onChange("url", text)}
        editable={editable}
        autoCapitalize="none"
        autoCorrect={false}
      />

      <FormTextField
        label="Notes"
        value={value.notes}
        onChangeText={(text) => onChange("notes", text)}
        editable={editable}
        multiline
        numberOfLines={3}
      />

      <FormTextField
        label="UpdatedAt"
        value={value.updatedAt}
        editable={false}
      />
    </View>
  );
}

// xxxxx Hook xxxxx

export function usePasswordForm() {
  const [state, setState] = useState<PasswordFormValue>({
    domain: "",
    username: "",
    password: "",
    url: "",
    notes: "",
    updatedAt: "",
  });

  function updateField<K extends keyof PasswordFormValue>(
    field: K,
    value: PasswordFormValue[K],
  ) {
    setState((current) => ({
      ...current,
      [field]: value,
    }));
  }

  return {
    state,
    setState,
    updateField,
  };
}

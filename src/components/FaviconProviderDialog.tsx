import {
  FAVICON_PROVIDERS,
  type FaviconProvider,
} from "@/libs/favicon";
import { setFaviconProvider, useFaviconProvider } from "@/libs/favicon_setting";
import { Dialog, Portal, RadioButton, Text, useTheme } from "react-native-paper";

type Props = {
  visible: boolean;
  onDismiss: () => void;
};

/** Pick where website icons come from (or turn the download off). */
export default function FaviconProviderDialog({ visible, onDismiss }: Props) {
  const theme = useTheme();
  const provider = useFaviconProvider();

  return (
    <Portal>
      <Dialog visible={visible} onDismiss={onDismiss}>
        <Dialog.Title>Website icons</Dialog.Title>

        <Dialog.Content>
          <Text
            variant="bodySmall"
            style={{ color: theme.colors.onSurfaceVariant, marginBottom: 8 }}
          >
            Icons are downloaded once and kept on this device. The chosen
            service sees the website names of your passwords.
          </Text>

          <RadioButton.Group
            value={provider}
            onValueChange={(value) => {
              setFaviconProvider(value as FaviconProvider);
              onDismiss();
            }}
          >
            {FAVICON_PROVIDERS.map((item) => (
              <RadioButton.Item
                key={item.id}
                value={item.id}
                label={item.label}
                position="leading"
                labelVariant="titleSmall"
                style={{ paddingHorizontal: 0 }}
              />
            ))}
          </RadioButton.Group>
        </Dialog.Content>
      </Dialog>
    </Portal>
  );
}

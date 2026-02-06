import { ThemeProvider } from "@/components/theme-provider";
import { AppRouter } from "@/components/app-router";

export default function App() {
  return (
    <ThemeProvider attribute="class" defaultTheme="system" enableSystem>
      <AppRouter />
    </ThemeProvider>
  );
}

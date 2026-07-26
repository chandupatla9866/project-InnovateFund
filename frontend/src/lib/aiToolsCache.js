const PREFIX = 'innovatefund:ai-tools:';

export function loadAiToolCache(key) {
  try {
    const raw = localStorage.getItem(PREFIX + key);
    return raw ? JSON.parse(raw) : undefined;
  } catch {
    return undefined;
  }
}

export function saveAiToolCache(key, value) {
  try {
    localStorage.setItem(PREFIX + key, JSON.stringify(value));
  } catch {
    // localStorage unavailable or quota exceeded — persistence is a convenience, not critical
  }
}

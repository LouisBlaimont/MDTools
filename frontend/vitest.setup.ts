import { beforeAll, vi } from 'vitest';

// Mock scrollIntoView so tests don't error
beforeAll(() => {
  HTMLElement.prototype.scrollIntoView = vi.fn();
});

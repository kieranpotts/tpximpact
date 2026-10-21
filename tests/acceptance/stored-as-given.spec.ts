import { test } from '@playwright/test'

// Requirement: R7
test.describe('Destination URLs are stored as given', () => {
  // A placeholder until the API service redirects (T04), which is the first
  // way to see a stored destination URL through the public interface. The
  // unit and integration tests cover R7 meanwhile.
  //
  // To fill in: shorten a URL with surrounding whitespace and unusual but
  // valid parts, such as `  HTTPS://Example.COM:443/a/../b?x=%7E#Top  `,
  // then follow the short URL, and expect a `Location` header of the trimmed
  // URL, otherwise unchanged.
  test('The API service redirects to the trimmed URL, otherwise unchanged', () => {
    test.fixme()
  })
})

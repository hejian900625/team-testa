import { expect, test } from './fixtures'

test.beforeEach(async ({ page }) => {
  await page.goto('/agents')
})

test('opens create form via button', async ({ page }) => {
  await page.getByTestId('create-agent').click()
  await page.waitForURL(/\/agents\/new/)
  await expect(page.getByTestId('agent-form-page')).toBeVisible()
})

test('shows validation errors when name and model are empty', async ({ page }) => {
  await page.getByTestId('create-agent').click()
  await page.waitForURL(/\/agents\/new/)
  await page.getByTestId('submit-agent').click()
  await expect(page.getByTestId('error-name')).toBeVisible()
  await expect(page.getByTestId('error-model')).toBeVisible()
})

test('creates agent successfully and redirects to detail', async ({ page }) => {
  await page.getByTestId('create-agent').click()
  await page.waitForURL(/\/agents\/new/)
  await page.getByTestId('input-name').fill('E2E Agent')
  await page.getByTestId('input-model').fill('gpt-4o')
  await page.getByTestId('input-description').fill('E2E 测试用')
  await page.getByTestId('submit-agent').click()
  await page.waitForURL(/\/agents\//)
  await expect(page.getByTestId('agent-detail-page')).toBeVisible()
  await expect(page.getByTestId('agent-name')).toContainText('E2E Agent')
})

test('cancel button returns to list', async ({ page }) => {
  await page.getByTestId('create-agent').click()
  await page.waitForURL(/\/agents\/new/)
  await page.getByTestId('cancel-agent').click()
  await page.waitForURL(/\/agents/)
  await expect(page.getByTestId('agent-list-page')).toBeVisible()
})

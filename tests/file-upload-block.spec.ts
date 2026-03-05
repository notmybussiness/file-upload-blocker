import { test, expect } from '@playwright/test';
import * as path from 'path';

test('File Upload and Block E2E Scenario', async ({ page, request }) => {
  const filePath = path.join(__dirname, 'fixtures', '서버 개발자 과제.pdf');
  const upperFilePath = path.join(__dirname, 'fixtures', '서버 개발자 과제_Upper.PDF');
  const exeFilePath = path.join(__dirname, 'fixtures', 'test.exe');

  // Clean up 'pdf' custom extension just in case previous test left it there
  await test.step('Setup: 기존 pdf 차단 정책 초기화 및 고정 확장자 체크 해제', async () => {
    // 1. Get current custom extensions
    const response = await request.get('/api/v1/admin/extensions/policy');
    if (response.ok()) {
        const policy = await response.json();
        const pdfItem = policy.custom.items.find((i: any) => i.name === 'pdf');
        if (pdfItem) {
            await request.delete(`/api/v1/admin/extensions/custom/${pdfItem.id}`);
        }
        
        // Ensure exe is unchecked
        await request.patch('/api/v1/admin/extensions/fixed/exe', { data: { checked: false } });
    }
  });

  await test.step('Client: 파일 정상 업로드 검증', async () => {
    await page.goto('/client/index.html');
    await page.setInputFiles('#upload-file-input', filePath);
    await page.click('#upload-btn');
    
    // Check if success message is displayed
    await expect(page.locator('#upload-result')).toContainText('업로드 성공', { timeout: 10000 });
  });

  await test.step('Client: 다운로드 동작 검증', async () => {
    const row = page.locator('#file-list tr').filter({ hasText: '서버 개발자 과제.pdf' }).first();
    await expect(row).toBeVisible({ timeout: 10000 });

    const downloadPromise = page.waitForEvent('download');
    await row.locator('a:has-text("다운로드")').click();
    const download = await downloadPromise;
    
    expect(download.suggestedFilename()).toBe('서버 개발자 과제.pdf');
    // Read the stream totally to confirm download complete
    await download.saveAs(path.join(__dirname, '..', 'test-results', download.suggestedFilename()));
  });

  await test.step('Admin: pdf 확장자 차단 설정', async () => {
    await page.goto('/admin/index.html');
    await page.fill('#custom-input', 'pdf');
    await page.click('#add-custom-btn');
    
    // Validate tag is added
    await expect(page.locator('#custom-tags')).toContainText('pdf', { timeout: 10000 });
  });

  await test.step('Client: 차단된 파일 업로드 실패 검증', async () => {
    await page.goto('/client/index.html');
    await page.setInputFiles('#upload-file-input', filePath);
    await page.click('#upload-btn');
    
    // Error message should be populated
    await expect(page.locator('#error-message')).not.toBeEmpty({ timeout: 10000 });
    
    // Upload result should be empty (meaning it failed before success)
    await expect(page.locator('#upload-result')).toBeEmpty();
  });

  await test.step('Client: 대소문자 다른 차단된 파일(PDF) 업로드 실패 검증', async () => {
    await page.goto('/client/index.html');
    await page.setInputFiles('#upload-file-input', upperFilePath);
    await page.click('#upload-btn');
    
    // Error message should be populated
    await expect(page.locator('#error-message')).not.toBeEmpty({ timeout: 10000 });
  });

  await test.step('Admin: 고정 확장자(exe) 차단 설정', async () => {
    await page.goto('/admin/index.html');
    
    // Check exe fixed extension (using ID defined in UI)
    const exeCheckbox = page.locator('#fixed-exe');
    if (!(await exeCheckbox.isChecked())) {
        await exeCheckbox.check();
    }
  });

  await test.step('Client: 고정 확장자(exe)로 차단된 파일 업로드 실패 검증', async () => {
    await page.goto('/client/index.html');
    await page.setInputFiles('#upload-file-input', exeFilePath);
    await page.click('#upload-btn');
    
    // Error message should be populated
    await expect(page.locator('#error-message')).not.toBeEmpty({ timeout: 10000 });
  });
});

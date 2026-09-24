

INSERT INTO STATUS VALUES (ssm_status_seq.NEXTVAL, '999', 'GENERAL_ERROR','خطای عمومی سیستم');
-- PIN errors
INSERT INTO STATUS VALUES (ssm_status_seq.NEXTVAL, '1001', 'PIN_GENERATION_FAILED','تولید رمز اول ناموفق بود');

INSERT INTO STATUS VALUES (ssm_status_seq.NEXTVAL, '1002', 'PIN_VERIFICATION_FAILED','اعتبارسنجی رمز اول ناموفق بود');

-- OTP errors
INSERT INTO STATUS VALUES (ssm_status_seq.NEXTVAL, '1003', 'OTP_GENERATION_FAILED','تولید رمز پویا ناموفق بود');

INSERT INTO STATUS VALUES (ssm_status_seq.NEXTVAL, '1004', 'OTP_VERIFICATION_FAILED','اعتبارسنجی رمز پویا ناموفق بود');


-- CVV2 errors
INSERT INTO STATUS VALUES (ssm_status_seq.NEXTVAL, '1005', 'CVV2_GENERATION_FAILED','تولید CVV2 ناموفق بود');

INSERT INTO STATUS VALUES (ssm_status_seq.NEXTVAL, '1006', 'CVV2_VERIFICATION_FAILED','اعتبارسنجی CVV2 ناموفق بود');


-- Vault / third-party integration
INSERT INTO STATUS VALUES (ssm_status_seq.NEXTVAL, '1007', 'VAULT_INVALID_RESPONSE','پاسخ دریافتی از Vault نامعتبر است');

INSERT INTO STATUS VALUES (ssm_status_seq.NEXTVAL, '1008', 'VAULT_UNEXPECTED_ERROR','خطای پیش‌بینی‌نشده در Vault رخ داد');
-- Session errors
INSERT INTO STATUS VALUES (ssm_status_seq.NEXTVAL, '1009', 'ACTIVE_SESSION_EXISTS','یک سشن فعال از قبل وجود دارد');
-- Status management errors
INSERT INTO STATUS VALUES (ssm_status_seq.NEXTVAL, '1010', 'STATUS_ALREADY_EXISTS','وضعیت از قبل ثبت شده است');

INSERT INTO STATUS VALUES (ssm_status_seq.NEXTVAL, '1011', 'STATUS_SAVE_FAILED','ذخیره وضعیت با خطا مواجه شد');

INSERT INTO STATUS VALUES (ssm_status_seq.NEXTVAL, '1012', 'STATUS_UPDATE_FAILED','به‌روزرسانی وضعیت با خطا مواجه شد');

INSERT INTO STATUS VALUES (ssm_status_seq.NEXTVAL, '1013', 'STATUS_LIST_FAILED','دریافت لیست وضعیت‌ها با خطا مواجه شد');

INSERT INTO STATUS VALUES (ssm_status_seq.NEXTVAL, '1014', 'ID_NOT_FOUND','دیتای مورد نظر یافت نشد');

INSERT INTO STATUS VALUES (ssm_status_seq.NEXTVAL, '1015', 'USER_HAS_NOT_PERMISSION','کاربر دسترسی لازم را ندارد');

INSERT INTO STATUS VALUES (ssm_status_seq.NEXTVAL, '1016', 'SESSION_NOT_VALID','توکن معتبر نیست');

INSERT INTO STATUS VALUES (ssm_status_seq.NEXTVAL, '1017', 'INPUT_PARAMETER_NOT_VALID','پارامترهای ورودی نامعتبر هستند');

INSERT INTO STATUS VALUES (ssm_status_seq.NEXTVAL, '1018', 'RATE_LIMIT_EXCEEDED','تعداد درخواست بیش از حد مجاز است');
INSERT INTO STATUS VALUES (ssm_status_seq.NEXTVAL, '1019', 'ACTIVE_OTP_EXIST','رمز پویا فعال موجود است');
INSERT INTO STATUS VALUES (ssm_status_seq.NEXTVAL, '1020', 'REQUESTED_CVV2_NOT_EXISTED','اطلاعات cvv2 درخواستی موجود نیست');
INSERT INTO STATUS VALUES (ssm_status_seq.NEXTVAL, '1021', 'CARD_IS_LOCKED','کارت مسدود شده است');
commit;

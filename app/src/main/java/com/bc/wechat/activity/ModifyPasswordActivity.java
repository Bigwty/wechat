package com.bc.wechat.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bc.wechat.R;
import com.bc.wechat.utils.StatusBarUtil;

import static com.bc.wechat.utils.ValidateUtil.validatePassword;

/**
 * 设置密码
 *
 * @author zhou
 */
public class ModifyPasswordActivity extends BaseActivity implements View.OnClickListener {

    private TextView mCompleteTv;

    private EditText mOldPasswordEt;
    private EditText mNewPasswordEt;
    private EditText mConfirmPasswordEt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_password);
        initStatusBar();
        StatusBarUtil.setStatusBarColor(ModifyPasswordActivity.this, R.color.status_bar_color_white);

        initView();
    }

    public void back(View view) {
        finish();
    }

    private void initView() {
        mCompleteTv = findViewById(R.id.tv_complete);
        mOldPasswordEt = findViewById(R.id.et_old_password);
        mNewPasswordEt = findViewById(R.id.et_new_password);
        mConfirmPasswordEt = findViewById(R.id.et_confirm_password);

        mCompleteTv.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_complete:
                validateForm();
                break;
        }
    }

    /**
     * 校验提交表单
     */
    private void validateForm() {
        String oldPassword = mOldPasswordEt.getText().toString();
        String newPassword = mNewPasswordEt.getText().toString();
        String confirmPassword = mConfirmPasswordEt.getText().toString();
        if (TextUtils.isEmpty(oldPassword)) {
            showAlertDialog(ModifyPasswordActivity.this, getString(R.string.tips),
                    getString(R.string.old_password_empty), getString(R.string.ok));
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            showAlertDialog(ModifyPasswordActivity.this, getString(R.string.tips),
                    getString(R.string.new_password_empty), getString(R.string.ok));
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            showAlertDialog(ModifyPasswordActivity.this, getString(R.string.tips),
                    getString(R.string.confirm_password_empty), getString(R.string.ok));
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlertDialog(ModifyPasswordActivity.this, getString(R.string.tips),
                    getString(R.string.confirm_password_incorrect), getString(R.string.ok));
            return;
        }

        if (!validatePassword(newPassword)) {
            showAlertDialog(ModifyPasswordActivity.this, getString(R.string.tips),
                    getString(R.string.password_rules), getString(R.string.ok));
            return;
        }

    }
}

package com.zkz.home.component;

import com.zkz.common.api.config.ProjectConfig;
import com.zkz.common.api.util.EncryptionUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class MyPasswordEncoder implements PasswordEncoder {
    public MyPasswordEncoder() {
    }

    @Override
    public boolean matches(CharSequence charSequence, String s) {


        String ss = charSequence.toString();
        return s.equals(EncryptionUtil.RSAEnc(ProjectConfig.PASSRSAPRI, ss));
    }

    @Override
    public String encode(CharSequence charSequence) {
        if (charSequence.equals("userNotFoundPassword")) {
            return null;
        }


        String s = EncryptionUtil.RSAEnc(ProjectConfig.PASSRSAPRI, (String) charSequence);
        return s;
    }
}

package com.findbulous.pos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.findbulous.pos.databinding.OutletPageBinding;

import io.realm.Realm;

public class OutletPage extends AppCompatActivity {

    private OutletPageBinding binding;

    private Realm realm;

    private String statuslogin;
    private Context contextpage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contextpage = OutletPage.this;
        binding = DataBindingUtil.setContentView(this, R.layout.outlet_page);

        realm = Realm.getDefaultInstance();


        binding.tempBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contextpage, ChoosePOSPermissionPage.class);
                startActivity(intent);
                finish();
            }
        });

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(contextpage, LoginPage.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
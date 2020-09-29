package com.example.bt;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class BTlogin extends StringRequest {
    final static private String ULR = "https://woo787.cafe24.com/UserLogin2.php";

    private Map<String, String> map;

    public BTlogin(String userPassword, Response.Listener<String> listener){
        super(Method.POST, ULR, listener, null);

        map = new HashMap<>();
        map.put("userPassword",userPassword);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;


    }
}

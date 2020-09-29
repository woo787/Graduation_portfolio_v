package com.example.bt;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class RegisterRequest extends StringRequest {

    final static private String ULR = "https://woo787.cafe24.com/UserRegister1.php";
    private Map<String, String>map;

    public RegisterRequest(String userID, String userPassword, String userEmail, Response.Listener<String> listener){
        super(Method.POST, ULR, listener, null);

        map = new HashMap<>();
        map.put("userID",userID);
        map.put("userPassword",userPassword);
        map.put("userEmail",userEmail);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}

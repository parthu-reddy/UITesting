package com.fooddelivery.e2e.util;

import com.microsoft.playwright.Page;
import java.util.*;

/** Same-origin gateway requests using the page's own session; never returns or logs its token. */
public final class GatewayApi {
    private GatewayApi() { }
    public record Response(int status,Object body) {
        public Map<?,?> object(){if(body instanceof Map<?,?> value){return value;}throw new AssertionError("Expected a JSON object, HTTP "+status);}
        /** The `data` object of the platform ApiResponse envelope. */
        public Map<?,?> data(){if(object().get("data") instanceof Map<?,?> value){return value;}throw new AssertionError("Expected an ApiResponse data object, HTTP "+status);}
    }
    public static Response get(Page page,String path){return request(page,"GET",path,null);}
    public static Response post(Page page,String path,Object body){return request(page,"POST",path,body);}
    public static Response put(Page page,String path,Object body){return request(page,"PUT",path,body);}
    public static Response patch(Page page,String path,Object body){return request(page,"PATCH",path,body);}
    public static Response delete(Page page,String path){return request(page,"DELETE",path,null);}
    private static Response request(Page page,String method,String path,Object body){
        if(path==null || !path.startsWith("/api/v1/") || path.contains("\\")){throw new IllegalArgumentException("Use a same-origin /api/v1/ gateway path");}
        Map<String,Object> args=new LinkedHashMap<>();args.put("method",method);args.put("path",path);args.put("body",body);
        var result=(Map<?,?>)page.evaluate("""
            async ({method,path,body}) => {
              const token=localStorage.getItem('auth_token');
              if(!token) throw new Error('A signed-in page is required');
              const headers={Authorization:'Bearer '+token};
              if(body!==null) headers['Content-Type']='application/json';
              const response=await fetch(path,{method,headers,credentials:'same-origin',redirect:'error',
                signal:AbortSignal.timeout(15000),body:body===null?undefined:JSON.stringify(body)});
              const text=await response.text();
              return {status:response.status,body:text ? JSON.parse(text) : null};
            }
            """,args);
        return new Response(((Number)result.get("status")).intValue(),result.get("body"));
    }
}

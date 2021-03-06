/**
 *  Copyright (C) Zhang,Yuexiang (xfeep)
 *
 */
package nginx.clojure;

import static nginx.clojure.Constants.BYTE_ARRAY_OFFSET;
import static nginx.clojure.Constants.DEFAULT_ENCODING;
import static nginx.clojure.Constants.KNOWN_REQ_HEADERS;
import static nginx.clojure.Constants.NGX_HTTP_CLOJURE_HEADERSI_COOKIE_OFFSET;
import static nginx.clojure.Constants.NGX_HTTP_CLOJURE_REQ_HEADERS_IN_OFFSET;
import static nginx.clojure.Constants.NGX_HTTP_CLOJURE_TEL_VALUE_OFFSET;
import static nginx.clojure.NginxClojureRT.UNSAFE;
import static nginx.clojure.NginxClojureRT.fetchNGXString;
import static nginx.clojure.NginxClojureRT.ngx_http_clojure_mem_get_header;
import static nginx.clojure.NginxClojureRT.ngx_http_clojure_mem_get_obj_addr;

import java.nio.charset.Charset;

public class RequestKnownHeaderFetcher implements RequestVarFetcher {
	
	public static RequestVarFetcher cookieFetcher = new RequestKnownNameVarFetcher("http_cookie");
	
	private long offset;
	private String name;
	
	public RequestKnownHeaderFetcher(String name) {
		assert name != null;
		Long p = KNOWN_REQ_HEADERS.get(name);
		if (p == null || p.longValue() == -1) {
			offset = -1;
		}else {
			offset += NGX_HTTP_CLOJURE_REQ_HEADERS_IN_OFFSET;
			offset += p.longValue();
		}
		this.name = name;
	}
	

	@Override
	public Object fetch(long r, Charset encoding) {
		if (offset != -1) {
			if (offset == NGX_HTTP_CLOJURE_REQ_HEADERS_IN_OFFSET + NGX_HTTP_CLOJURE_HEADERSI_COOKIE_OFFSET ) {
				return cookieFetcher.fetch(r, encoding);
			}else {
				long haddr = UNSAFE.getAddress(r + offset);
				if (haddr == 0){
					return null;
				}
				return fetchNGXString(haddr + NGX_HTTP_CLOJURE_TEL_VALUE_OFFSET, DEFAULT_ENCODING);
			}
		}else {
			byte[] kbs = name.getBytes();
			long headersPointer = r + NGX_HTTP_CLOJURE_REQ_HEADERS_IN_OFFSET;
			long hp = ngx_http_clojure_mem_get_header(headersPointer, ngx_http_clojure_mem_get_obj_addr(kbs) + BYTE_ARRAY_OFFSET , kbs.length);
			if (hp == 0){
				return null;
			}else {
				return fetchNGXString(hp + NGX_HTTP_CLOJURE_TEL_VALUE_OFFSET, DEFAULT_ENCODING);
			}
		}

	}

}

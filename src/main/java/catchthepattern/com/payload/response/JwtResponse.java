package catchthepattern.com.payload.response;

import java.util.Date;
import java.util.List;

public class JwtResponse {
	  private String token;
	  private String type = "Bearer";
	  private Long id;
	  private String username;
	  private String email;
	  private List<String> roles;
	  private Date serviceEndDate;
	  private Date lastPaidDate;
	  private String promoCode;
	  private String sharePromoCode;
	  private boolean isPromoUsed;
	  private String receiveEmails;

      public JwtResponse(String token, String type, Long id, String username, String email, List<String> roles,
              Date serviceEndDate, Date lastPaidDate, String promoCode, String sharePromoCode, boolean isPromoUsed, String receiveEmails) {
          super();
          this.token = token;
          this.type = type;
          this.id = id;
          this.username = username;
          this.email = email;
          this.roles = roles;
          this.serviceEndDate = serviceEndDate;
          this.lastPaidDate = lastPaidDate;
          this.promoCode = promoCode;
          this.sharePromoCode = sharePromoCode;
          this.isPromoUsed = isPromoUsed;
          this.receiveEmails = receiveEmails;
      }

	  public String getAccessToken() {
	    return token;
	  }



    public void setAccessToken(String accessToken) {
	    this.token = accessToken;
	  }

	  public String getTokenType() {
	    return type;
	  }

	  public void setTokenType(String tokenType) {
	    this.type = tokenType;
	  }

	  public Long getId() {
	    return id;
	  }

	  public void setId(Long id) {
	    this.id = id;
	  }

	  public String getUsername() {
	    return username;
	  }

	  public void setUsername(String username) {
	    this.username = username;
	  }

	  public List<String> getRoles() {
	    return roles;
	  }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getServiceEndDate() {
        return serviceEndDate;
    }

    public void setServiceEndDate(Date serviceEndDate) {
        this.serviceEndDate = serviceEndDate;
    }

    public Date getLastPaidDate() {
        return lastPaidDate;
    }

    public void setLastPaidDate(Date lastPaidDate) {
        this.lastPaidDate = lastPaidDate;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public boolean isPromoUsed() {
        return isPromoUsed;
    }

    public void setPromoUsed(boolean isPromoUsed) {
        this.isPromoUsed = isPromoUsed;
    }

    public String getSharePromoCode() {
        return sharePromoCode;
    }

    public void setSharePromoCode(String sharePromoCode) {
        this.sharePromoCode = sharePromoCode;
    }

    public String getReceiveEmails() {
        return receiveEmails;
    }

    public void setReceiveEmails(String receiveEmails) {
        this.receiveEmails = receiveEmails;
    }
	  
	  
}

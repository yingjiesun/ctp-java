package catchthepattern.com.models;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonFormat;

import catchthepattern.com.afilters.AfilterSet;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "Users")
public class User implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable=false, unique=true)
	private String username;
	
	@Column(nullable=false, length=64)
	private String password;
	
	private UserRole role = UserRole.FREE;
	private Boolean locked = false;	
	private Boolean enabled = false;	
	private boolean accountNonLocked;
	private String promoCode = ""; // unique code from other user's user name, to be used to upgrade this user
	private String sharePromoCode = ""; // unique code from this user's user name, to be shared 
	private boolean isPromoUsed = false; // if promoCode is used for this user, set isPromoUsed=true (paid $199)
	private String receiveEmails = "Y";
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
	private Date lastPaidDate;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
	private Date serviceEndDate;
	
	@OneToMany(mappedBy="user")
	private Set<Pattern> patterns;
	
	@OneToMany(mappedBy="user")
	private Set<AfilterSet> afilterSet;
	
	@OneToMany(mappedBy="user")
    private Set<CtpPayment> payments;
	
	public User() {}

	public User(Long id, String username, String password, UserRole role, boolean locked, boolean enabled, boolean accountNonLocked, Set<Pattern> patterns, Set<AfilterSet> afilterSet, String receiveEmails) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.role = role;
		this.locked = locked;
		this.enabled = enabled;
		this.accountNonLocked = accountNonLocked;
		this.patterns = patterns;
		this.afilterSet = afilterSet;
		this.receiveEmails = receiveEmails;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {

		final SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(role.name());
		return Collections.singletonList(simpleGrantedAuthority);
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return !locked;
	}
	
	public void setAccountNonLocked(Boolean accountNonLocked) { 
	      this.accountNonLocked = accountNonLocked; 
	   } 
	   public boolean getAccountNonLocked() { 
	      return accountNonLocked; 
	   } 

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
	

	public void setPassword(String encryptedPassword) {
		this.password = encryptedPassword;
		
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;		
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	
	public void setUsername (String username) {
		this.username = username;
	}

	public UserRole getUserRole() {
		return role;
	}

	public void setUserRole(UserRole role) {
		this.role = role;
	}

	public Boolean getLocked() {
		return locked;
	}

	public void setLocked(Boolean locked) {
		this.locked = locked;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Date getLastPaidDate() {
        return lastPaidDate;
    }

    public void setLastPaidDate(Date lastPaidDate) {
        this.lastPaidDate = lastPaidDate;
    }

    public Date getServiceEndDate() {
        return serviceEndDate;
    }

    public void setServiceEndDate(Date serviceEndDate) {
        this.serviceEndDate = serviceEndDate;
    }

    public Set<Pattern> getPatterns() {
        return patterns;
    }

    public void setPatterns(Set<Pattern> patterns) {
        this.patterns = patterns;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
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

    public Set<CtpPayment> getPayments() {
        return payments;
    }

    public void setPayments(Set<CtpPayment> payments) {
        this.payments = payments;
    }

    public String getSharePromoCode() {
        return sharePromoCode;
    }

    public void setSharePromoCode(String sharePromoCode) {
        this.sharePromoCode = sharePromoCode;
    }

    public Set<AfilterSet> getAfilterSet() {
        return afilterSet;
    }

    public void setAfilterSet(Set<AfilterSet> afilterSet) {
        this.afilterSet = afilterSet;
    }

    public String getReceiveEmails() {
        return receiveEmails;
    }

    public void setReceiveEmails(String receiveEmails) {
        this.receiveEmails = receiveEmails;
    }	
    
    
	
}
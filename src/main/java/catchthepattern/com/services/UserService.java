package catchthepattern.com.services;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import catchthepattern.com.models.User;
import catchthepattern.com.models.UserRole;
import catchthepattern.com.payload.response.MessageResponse;
import catchthepattern.com.registration.ConfirmationToken;
import catchthepattern.com.registration.PasswordResetToken;
import catchthepattern.com.repositories.PasswordTokenRepository;
import catchthepattern.com.repositories.UserRepository;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class UserService implements UserDetailsService {
	
	//Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Value("${catchthepattern.app.url}")
    private String APP_URL;
    
    @Value("${catchthepattern.app.server}")
    private String SERVER_URL;
    
    @Value("${catchthepattern.app.email}")
    private String APP_EMAIL;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private ConfirmationTokenService confirmationTokenService;
	
	@Autowired
	private EmailSenderService emailSenderService;
	
	@Autowired
	private PasswordTokenRepository passwordTokenRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {		
		
		
		User user = userRepository.findByUsername(email);		
		
		if (user != null) {
			//logger.info("USER FOUND!");	
		   // Authentication authentication= new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()) ; 
		   // SecurityContextHolder.getContext().setAuthentication(authentication);
			return user;
		}
		else {
			//logger.error("Throwing ERROR");
			throw new UsernameNotFoundException(MessageFormat.format("User with email {0} cannot be found.", email));
		}
	}	
	
	// seems this function is not used, registration is taken care in controller
	public ResponseEntity<?> signUpUser(User user) {
		//logger.debug("signUpUser() called");
		System.out.println("UserService signUpUser() called"); 
		
		if (userRepository.existsByUsername(user.getUsername())) {
		      return ResponseEntity
		          .badRequest()
		          .body(new MessageResponse("Error: Username is already taken!"));
		    }

		
		final String encryptedPassword = bCryptPasswordEncoder.encode(user.getPassword());
		user.setPassword(encryptedPassword);
		final User createdUser = userRepository.save(user);
		final ConfirmationToken confirmationToken = new ConfirmationToken(user);
		confirmationTokenService.saveConfirmationToken(confirmationToken);
		
		System.out.println(createdUser);
		sendConfirmationMail(createdUser.getUsername(), confirmationToken.getConfirmationToken());		
		sendMailToManagement(createdUser.getUsername(), "CTP - NEW REGISTRATION");
		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}
	
	public String updateReceiveEmails(String receiveEmails) {
	    Authentication auth;
	    User activeUser;
	    try {
          auth = SecurityContextHolder.getContext().getAuthentication();
          activeUser = userRepository.findByUsername(auth.getName());
          if (activeUser != null) {
              activeUser.setReceiveEmails(receiveEmails);
              userRepository.save(activeUser);
              return "Account updated.";
           } else {
              return "User not found.";
           }
          
         } catch (Error e) {
             System.out.println("EXCEPTION UserService updateReceiveEmails");
             return "EXCEPTION updateReceiveEmails";
         }
	} 
	
	
	public void confirmUser(ConfirmationToken confirmationToken) {
	//	logger.debug("confirmUser called");
	  final User user = confirmationToken.getUser();
	  user.setEnabled(true);
	  userRepository.save(user);
	  confirmationTokenService.deleteConfirmationToken(confirmationToken.getId());
	  sendGreetingMail(user.getUsername());
	  System.out.print("confirmed");
	}
	
	public void sendConfirmationMail(String userMail, String token) {

		final SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(userMail);
		mailMessage.setSubject("CatchThePattern.com - Activate your account");
		mailMessage.setFrom(APP_EMAIL);
		mailMessage.setText(
				"Thank you for registering with CatchThePattern.com. Please click on the below link to activate your account. " + SERVER_URL + "/confirm?token="
						+ token);
		emailSenderService.sendEmail(mailMessage);
	}
	
   public void sendGreetingMail(String userMail) {

        final SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(userMail);
        mailMessage.setSubject("CatchThePattern.com Account Confirmed!");
        mailMessage.setFrom(APP_EMAIL);
        mailMessage.setText(
                "Thank you for registering with CatchThePattern.com. You can now begin creating patterns, and the platform will automatically search for matching stocks every day."
                        );
        emailSenderService.sendEmail(mailMessage);
    }
	
	public void sendResetPasswordMail(String userMail, String token) {
	    final SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(userMail);
        mailMessage.setSubject("CatchThePattern.com - reset password");
        mailMessage.setFrom(APP_EMAIL);
        mailMessage.setText(
                "Please click on the below link to reset your password. " + APP_URL + "/resetPassword?token="
                        + token);
        emailSenderService.sendEmail(mailMessage);
	}
	
   public void sendContactMail(String msg) {
        final SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo("catchthepattern@gmail.com");
        mailMessage.setSubject("CatchThePattern.com - CONTACT");
        mailMessage.setFrom(APP_EMAIL);
        mailMessage.setText(msg);
        emailSenderService.sendEmail(mailMessage);
    }
   
   public void sendMailToManagement(String msg, String subject) {
       final SimpleMailMessage mailMessage = new SimpleMailMessage();
       mailMessage.setTo("catchthepattern@gmail.com");
       mailMessage.setSubject(subject);
       mailMessage.setFrom(APP_EMAIL);
       mailMessage.setText(msg);
       emailSenderService.sendEmail(mailMessage);
   }
	
	public void extendOneYear(User user) {
	    Date endDate = user.getServiceEndDate();
	    Date today = new Date();
	    if (endDate == null || endDate.before(today)) endDate = today;
	    Calendar calendar = Calendar.getInstance();	    
	    calendar.setTime(endDate);
	    calendar.add(Calendar.YEAR, 1);
	    Date serviceEndDate = calendar.getTime();
	    user.setServiceEndDate(serviceEndDate);
	    user.setLastPaidDate(new Date());
	    user.setRole(UserRole.PRO);
	    userRepository.save(user);	    
	}
	
	public void extend4Months(User user) {
	    Date endDate = user.getServiceEndDate();
        Date today = new Date();
        if (endDate == null || endDate.before(today)) endDate = today;
        Calendar calendar = Calendar.getInstance();     
        calendar.setTime(endDate);
        calendar.add(Calendar.MONTH, 4);
        Date serviceEndDate = calendar.getTime();
        user.setServiceEndDate(serviceEndDate);
        user.setLastPaidDate(new Date());
        user.setRole(UserRole.PRO);
        userRepository.save(user);      
	}
	
	public void extendOneMonth(User user) {
        Date endDate = user.getServiceEndDate();
        Date today = new Date();
        if (endDate == null || endDate.before(today)) endDate = today;
        Calendar calendar = Calendar.getInstance();     
        calendar.setTime(endDate);
        calendar.add(Calendar.MONTH, 1);
        Date serviceEndDate = calendar.getTime();
        user.setServiceEndDate(serviceEndDate);
        user.setLastPaidDate(new Date());
        user.setRole(UserRole.PRO);
        userRepository.save(user);      
    }
	
	   public void set3Months(User user) {
	        Date endDate = user.getServiceEndDate();
	        Date today = new Date();
	        if (endDate == null || endDate.before(today)) endDate = today;
	        Calendar calendar = Calendar.getInstance();     
	        calendar.setTime(endDate);
	        calendar.add(Calendar.MONTH, 3);
	        Date serviceEndDate = calendar.getTime();
	        user.setServiceEndDate(serviceEndDate);
	        user.setLastPaidDate(new Date());
	        user.setRole(UserRole.PRO);
	        userRepository.save(user);      
	    }
	
	public boolean isValidPromoCode(String promoCode) {
	    
	    return false;
	}
	
	public boolean isSuperAccout(String username) {
	    String superAcoounts = "yingjiesun+1@gmail.com,admin@gmail.com,yingjiesun@gmail.com";
	    return Arrays.asList(superAcoounts.split(",")).contains(username);
	}
	
	// return string of promoCode checking result to FE
	// input : sting from FE like D17777384834
	public String getDevPromoValStr(String promoCode) {
	    String dateStr = promoCode.substring(1); //remove 'D'
	    try {
	        long dateNum = Utils.decodePromo(Long.parseLong(dateStr));		        
	        String dateString = "" + dateNum;
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
	        LocalDate date = LocalDate.parse(dateString, formatter);
	        LocalDate today = LocalDate.now();	     
	        
	        if (!date.isBefore(today)) return "PRO 3 month. Expires on " + date;	        
	    } catch(Exception e) {
	        System.out.println("EXCEPTION UserService getDevPromoValStr()");
	        return "Invalid or expired promoCode";
	    }
	    return "Invalid or expired promoCode";
    }
	
	// Create resetpassword token and save to DB
    /*
     * public void createPasswordResetTokenForUser(User user, String token) {
     * PasswordResetToken myToken = new PasswordResetToken(token, user);
     * passwordTokenRepository.save(myToken);
     * }
     */
	
	public User getActiveUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
        	return userRepository.findByUsername(auth.getName());
        } else {
        	return null;
        }
    }
	
	
}
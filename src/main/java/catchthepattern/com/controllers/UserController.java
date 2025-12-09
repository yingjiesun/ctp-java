package catchthepattern.com.controllers;

import java.net.URLDecoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import catchthepattern.com.models.User;
import catchthepattern.com.models.UserRole;
import catchthepattern.com.payload.request.LoginRequest;
import catchthepattern.com.payload.response.JwtResponse;
import catchthepattern.com.payload.response.MessageResponse;
import catchthepattern.com.registration.ConfirmationToken;
import catchthepattern.com.registration.PasswordDto;
import catchthepattern.com.repositories.UserRepository;
import catchthepattern.com.security.JwtUtils;
import catchthepattern.com.services.ConfirmationTokenService;
import catchthepattern.com.services.EmailSenderService;
import catchthepattern.com.services.UserService;
import catchthepattern.com.services.Utils;
import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin
@RestController
public class UserController {	
    
    @Value("${catchthepattern.app.url}")
    private String APP_URL;
		
	@Autowired
	AuthenticationManager authenticationManager;
	
	@Autowired
	JwtUtils jwtUtils;

	@Autowired
	private UserService userService;
	// private final UserService userService = new UserService();
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ConfirmationTokenService confirmationTokenService;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
    private EmailSenderService emailSenderService;


	@GetMapping("/")
	String home() {
		return "welcome";
	}
	
	 @RequestMapping("/user")
	 @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	  public User user(User user) {
	    return user;
	  }	 
	 
	  @PostMapping("/signin")
	  public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
	    Authentication authentication = authenticationManager.authenticate(
	        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

	    SecurityContextHolder.getContext().setAuthentication(authentication);
	    String jwt = jwtUtils.generateJwtToken(authentication);
	    
	    User userDetails = (User) authentication.getPrincipal(); 
	    
	    Date today = new Date();
	    if (userDetails.getServiceEndDate() != null && userDetails.getServiceEndDate().before(today)) {
	        userDetails.setRole(UserRole.FREE);
	        userRepository.save(userDetails);
	    }	
	    
	    String sharePromoCode = "SHARE_PRMOCODE";
	    
	    try {
	        sharePromoCode = "" + Utils.encodePromo(userDetails.getId());	
	    } catch(Exception e) {System.out.println(e);}
	    
	    if (userService.isSuperAccout(userDetails.getUsername())) {	    
	        LocalDate currentDate = LocalDate.now();
	        LocalDate oneMonthLater = currentDate.plusMonths(1);
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
	        String dateString = oneMonthLater.format(formatter);	        
	        sharePromoCode += " https://catchthepattern.com/register?promoCode=D" +  Utils.encodePromo(Long.parseLong(dateString));
	    }
	    
	   // List<String> roles = userDetails.getAuthorities().stream()
	    //    .map(item -> item.getAuthority())
	    //    .collect(Collectors.toList()); 
	    
	    List<String> roles = new ArrayList<>();
	    for (GrantedAuthority item : userDetails.getAuthorities()) {
	        roles.add(item.getAuthority());
	    }

	    return ResponseEntity.ok(new JwtResponse(
	         jwt, 
	         null,
             userDetails.getId(), 
             userDetails.getUsername(), 
             userDetails.getUsername(), // user name is email
             roles,             
             userDetails.getServiceEndDate(),
             userDetails.getLastPaidDate(),
             userDetails.getPromoCode(),
             sharePromoCode,
             userDetails.isPromoUsed(),
             userDetails.getReceiveEmails()
          ));
	  } 
	
	@PostMapping("/register") 
	@ResponseBody
	ResponseEntity<?> register(@RequestBody User user) {
	 
		
		if (userRepository.existsByUsername(user.getUsername())) {
		      return ResponseEntity
		          .badRequest()
		          .body(new MessageResponse("The user name '" + user.getUsername() + "' has been registered."));
		}
		
		final String encryptedPassword = bCryptPasswordEncoder.encode(user.getPassword());
		user.setPassword(encryptedPassword);
		
		Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        // Add 1 month to the current date
        calendar.add(Calendar.MONTH, 1);

        // Get the date of the same day in the next month as a java.util.Date object
        Date dateNextMonth = calendar.getTime();
		
		user.setServiceEndDate(dateNextMonth); // one month free
		
		final User createdUser = userRepository.save(user);
		final ConfirmationToken confirmationToken = new ConfirmationToken(user);
		confirmationTokenService.saveConfirmationToken(confirmationToken);		
		
		userService.sendConfirmationMail(createdUser.getUsername(), confirmationToken.getConfirmationToken());
		userService.sendMailToManagement(createdUser.getUsername(), "CTP - NEW REGISTRATION");		
		
		if (user.getPromoCode() != null && user.getPromoCode().length() > 0 && userService.getDevPromoValStr(user.getPromoCode()).contains("Expires")) {
		    userService.set3Months(createdUser);
        }
		
		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	  
	}
	
	@PostMapping("/subscribe") 
	@ResponseBody
	ResponseEntity<?> subscribe(@RequestBody User user) {
	 
		
		if (userRepository.existsByUsername(user.getUsername())) {
		      return ResponseEntity
		          .badRequest()
		          .body(new MessageResponse("The user name '" + user.getUsername() + "' has been registered."));
		}
		
		final String encryptedPassword = bCryptPasswordEncoder.encode(user.getPassword());
		user.setPassword(encryptedPassword);
		
		Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        // Add 1 month to the current date
        calendar.add(Calendar.MONTH, 1);

        // Get the date of the same day in the next month as a java.util.Date object
        Date dateNextMonth = calendar.getTime();
		
		user.setServiceEndDate(dateNextMonth); // one month free
		
		final User createdUser = userRepository.save(user);
		final ConfirmationToken confirmationToken = new ConfirmationToken(user);
		confirmationTokenService.saveConfirmationToken(confirmationToken);		
		
		userService.sendConfirmationMail(createdUser.getUsername(), confirmationToken.getConfirmationToken());
		userService.sendMailToManagement(createdUser.getUsername(), "CTP - NEW REGISTRATION");		
		
		if (user.getPromoCode() != null && user.getPromoCode().length() > 0 && userService.getDevPromoValStr(user.getPromoCode()).contains("Expires")) {
		    userService.set3Months(createdUser);
        }
		
		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	  
	}
	
	
	  
	 @GetMapping("/confirm") 
	 String confirmMail(@RequestParam("token") String token) {		 
		 Optional<ConfirmationToken> optionalConfirmationToken = confirmationTokenService.findConfirmationTokenByToken(token);		
		 optionalConfirmationToken.ifPresent(userService::confirmUser);
		 return "Account confirmed. Please visit www.catchThePattern.com to login."; 
	 }
	 
	 private String getErrorMessage(HttpServletRequest request, String key) {
	      Exception exception = (Exception) request.getSession().getAttribute(key); 
	      String error = ""; 
	      if (exception instanceof BadCredentialsException) { 
	         error = "Invalid username and password!"; 
	      } else if (exception instanceof LockedException) { 
	         error = exception.getMessage(); 
	      } else { 
	         error = "Invalid username and password!"; 
	      } 
	      return error;
	   }	 
	 
	   @GetMapping("/open/resetPasswordRequest") 
	   ResponseEntity<?> resetPassword(@RequestParam("userEmail") String userEmail) throws Exception {  
           /*
            * String decodedEmail = URLDecoder.decode(userEmail, "UTF-8");
            * System.out.println("resetPasswordRequest decodedEmail: " + decodedEmail);
            */
	     User user = userRepository.findByUsername(userEmail); 
	     if (user == null) {
	        return ResponseEntity.ok(new MessageResponse("Email not found"));
	     }
	     String token = UUID.randomUUID().toString();
	     final ConfirmationToken confirmationToken = new ConfirmationToken(user);
	     confirmationTokenService.saveConfirmationToken(confirmationToken);
	     userService.sendResetPasswordMail(user.getUsername(), confirmationToken.getConfirmationToken());	
	     return ResponseEntity.ok(new MessageResponse("Reset password email sent"));
	  }
	   
	   @GetMapping("/register/validatePromoCode")
	    public String validatePromoCode(@RequestParam("promoCode") String promoCode) {
	       
	       if (promoCode.startsWith("D")) {	          
	           return userService.getDevPromoValStr(promoCode);
	       } else {
	           try {                    
	               long promoUserId = Utils.decodePromo(Long.parseLong(promoCode));               
	                          
	               User promoUser = userRepository.findById(promoUserId);
	               if (promoUser != null) return "Referral code"; // is referral promocode	               
	           } catch (Exception e) {}
	       }
	       
	        return "Invalid PromoCode";
	    }
	   
	   @PostMapping("user/modify")       
	   public ResponseEntity<?> updateUserReceiveEmails(@RequestBody String receiveEmails) {           
	       userService.updateReceiveEmails(receiveEmails); 
           return ResponseEntity.ok(new MessageResponse("Ok"));
       }
	   
	   

      /*
       * @GetMapping("/open/contact")
       * ResponseEntity<?> sendContactMail(@RequestParam("msg") String msg) throws
       * Exception {
       * userService.sendContactMail(msg);
       * return ResponseEntity.ok(new MessageResponse("Contact message sent"));
       * }
       */  
       @PostMapping("/open/contact")
       public ResponseEntity<?> sendContactMail(@RequestBody String inputString) {           
           userService.sendContactMail(inputString);   
           return ResponseEntity.ok(new MessageResponse("Contact email sent"));
       }
       
	 
	 // validate token and send user to reset password page
	 @GetMapping("/open/resetpassword")
	 public String showChangePasswordPage(@RequestParam("token") String token) {
	     boolean validToken;
	     Optional<ConfirmationToken> optionalConfirmationToken = confirmationTokenService.findConfirmationTokenByToken(token);        
         if (optionalConfirmationToken.isPresent()){
             return "Valid";
         } else {
             return "Invalid";
         }         
	 }
	 
	  @PostMapping("open/savePassword") 
	    @ResponseBody
	    ResponseEntity<?> savePassword(@RequestBody PasswordDto passwordDto) {
	      Optional<ConfirmationToken> optionalConfirmationToken = confirmationTokenService.findConfirmationTokenByToken(passwordDto.getToken());        
	      if (optionalConfirmationToken.isPresent()) {
	          final User user = optionalConfirmationToken.get().getUser();
	          final String encryptedPassword = bCryptPasswordEncoder.encode(passwordDto.getNewPassword());
	          user.setPassword(encryptedPassword);	         
	          userRepository.save(user);
	          confirmationTokenService.deleteConfirmationToken(optionalConfirmationToken.get().getId());
	          return ResponseEntity.ok(new MessageResponse("Password reset successfully!"));
	      }
	      return ResponseEntity.ok(new MessageResponse("User not found or invalid token."));	      
	    }

}
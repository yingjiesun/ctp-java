package catchthepattern.com.paypal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

import catchthepattern.com.models.CtpPayment;
import catchthepattern.com.models.User;
import catchthepattern.com.payload.response.MessageResponse;
import catchthepattern.com.repositories.CtpPaymentRepository;
import catchthepattern.com.repositories.UserRepository;
import catchthepattern.com.services.UserService;
import catchthepattern.com.services.Utils;
import jakarta.servlet.http.HttpServletRequest;

import io.github.cdimascio.dotenv.Dotenv;

@Service
public class PayPalClient {
    
    private boolean isDev = false;
    
    private Authentication auth;
    private User activeUser;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CtpPaymentRepository ctpPaymentRepository;    
      
    private String clientId_sandbox = dotenv.get("PAYPAL_CLIENT_ID_SANDBOX"); // Sandbox
    private String clientSecret_sandbox = dotenv.get("PAYPAL_CLIENT_ID_SANDBOX"); // Sandbox  
    private String clientId_prod = dotenv.get("PAYPAL_CLIENT_ID_PROD"); // Live
    private String clientSecret_prod = dotenv.get("PAYPAL_CLIENT_SECRET_PROD"); // Live   
    
    @Value("${catchthepattern.app.url}") 
    private String appUrl; 
    
    public Map<String, Object> createPayment(String sum){
        
        if (isDev) appUrl = "http://localhost:4210";        
        
        Map<String, Object> response = new HashMap<String, Object>();
        Amount amount = new Amount();
        amount.setCurrency("USD");
        amount.setTotal(sum);
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription("CatchThePattern.com - one year subscription");
        List<Transaction> transactions = new ArrayList<Transaction>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");
        

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(appUrl);
        
        redirectUrls.setReturnUrl(appUrl + "/paypalConfirm");       
        
        payment.setRedirectUrls(redirectUrls);
        Payment createdPayment;
        try {
            String redirectUrl = "";
            String clientId = isDev? clientId_sandbox : clientId_prod;
            String clientSecret = isDev? clientSecret_sandbox : clientSecret_prod;
            String paypalParam = isDev? "sandbox" : "live";
            APIContext context = new APIContext(clientId, clientSecret, paypalParam);
            
            createdPayment = payment.create(context);
            if(createdPayment!=null){
                List<Links> links = createdPayment.getLinks();
                for (Links link:links) {
                    if(link.getRel().equals("approval_url")){
                        redirectUrl = link.getHref();
                        break;
                    }
                }
                response.put("status", "success");
                response.put("redirect_url", redirectUrl);
            }
        } catch (PayPalRESTException e) {
            System.out.println("Error happened during payment creation!");
        }
        return response;
    }
    
    
 public Map<String, Object> createPayment_new(String username, String sum){
        
        if (isDev) appUrl = "http://localhost:4210";        
        
        Map<String, Object> response = new HashMap<String, Object>();
        Amount amount = new Amount();
        amount.setCurrency("USD");
        amount.setTotal(sum);
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription("CatchThePattern.com - one year subscription");
        List<Transaction> transactions = new ArrayList<Transaction>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");
        PayerInfo payerInfo = new PayerInfo();
        payerInfo.setEmail(username);
        payer.setPayerInfo(payerInfo);

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(appUrl);
        redirectUrls.setReturnUrl(appUrl + "/paypalConfirm");
        payment.setRedirectUrls(redirectUrls);
        Payment createdPayment;
        try {
            String redirectUrl = "";
            String clientId = isDev? clientId_sandbox : clientId_prod;
            String clientSecret = isDev? clientSecret_sandbox : clientSecret_prod;
            String paypalParam = isDev? "sandbox" : "live";
            APIContext context = new APIContext(clientId, clientSecret, paypalParam);
            
            createdPayment = payment.create(context);
            if(createdPayment!=null){
                List<Links> links = createdPayment.getLinks();
                for (Links link:links) {
                    if(link.getRel().equals("approval_url")){
                        redirectUrl = link.getHref();
                        break;
                    }
                }
                response.put("status", "success");
                response.put("redirect_url", redirectUrl);
            }
        } catch (PayPalRESTException e) {
            System.out.println("Error happened during payment creation!");
        }
        return response;
    }
    
    
    //public Map<String, Object> completePayment(HttpServletRequest req){
    public ResponseEntity<?> completePayment(HttpServletRequest req){
        /*
         * System.out.println("PayPalClient completePayment called!");
         * System.out.println(req.getParameter("paymentId"));
         * System.out.println(req.getParameter("payerId"));
         */
        if (isDev) appUrl = "http://localhost:4210";        
              
        Map<String, Object> response = new HashMap();
        Payment payment = new Payment();
        payment.setId(req.getParameter("paymentId"));
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(req.getParameter("payerId"));
        try {
            String clientId = isDev? clientId_sandbox : clientId_prod;
            String clientSecret = isDev? clientSecret_sandbox : clientSecret_prod;
            String paypalParam = isDev? "sandbox" : "live";
            APIContext context = new APIContext(clientId, clientSecret, paypalParam);
            Payment createdPayment = payment.execute(context, paymentExecution);
            if(createdPayment!=null){
                
                
                Amount amount = createdPayment.getTransactions().get(0).getAmount();
                Double paidAmount = Double.parseDouble(amount.getTotal());
                
               // System.out.println("paidAmount: " + paidAmount);
                
                response.put("status", "success");
                response.put("payment", createdPayment);
                
                auth = SecurityContextHolder.getContext().getAuthentication();
                activeUser = userRepository.findByUsername(auth.getName());
                
                CtpPayment ctpPayment = new CtpPayment();
                ctpPayment.setUser(activeUser);
                ctpPayment.setPay_id(createdPayment.getId());
                ctpPayment.setPayer_id(createdPayment.getPayer().getPayerInfo().getPayerId());
                ctpPayment.setPayer_email(createdPayment.getPayer().getPayerInfo().getEmail());
                ctpPayment.setPayer_last_name(createdPayment.getPayer().getPayerInfo().getLastName());
                ctpPayment.setPayer_first_name(createdPayment.getPayer().getPayerInfo().getFirstName());                
                ctpPayment.setAmount(Double.valueOf(createdPayment.getTransactions().get(0).getAmount().getTotal()));
                ctpPayment.setCurrency(createdPayment.getTransactions().get(0).getAmount().getCurrency());                
                ctpPayment.setCreate_time(createdPayment.getCreateTime());
                ctpPayment.setUpdate_time(createdPayment.getUpdateTime());
                ctpPaymentRepository.save(ctpPayment);
                
                // Now process user who shared promoCode
                if (paidAmount == 99.0 && activeUser.getPromoCode() != null && !activeUser.getPromoCode().equals("") && !activeUser.isPromoUsed()) {
                    
                    activeUser.setPromoUsed(true);
                    
                    try {                    
                        long promoUserId = Utils.decodePromo(Long.parseLong(activeUser.getPromoCode()));
                        
                       // System.out.println("promoUserStr: " + promoUserId);
                        
                        if ( activeUser.getId() != promoUserId) { // should not apply promo to himself
                            User promoUser = userRepository.findById(promoUserId);
                            userService.extend4Months(promoUser); // User saved in the function.
                        }
                    } catch (Exception e) {}
                    
                }
                
                if (paidAmount == 10.0) userService.extendOneMonth(activeUser);
                else userService.extendOneYear(activeUser); 
                
                System.out.println("Completed Payment !"); 
                
            }
        } catch (PayPalRESTException e) {
            System.err.println("PayPalClient completePayment EXCEPTION");
            System.err.println(e.getDetails());
        }
        // return response;// "Payment approved.";
        userService.sendMailToManagement(activeUser.getUsername(), "CTP - PAYMENT RECEIVED");
        return ResponseEntity.ok(new MessageResponse("Payment approved!"));
    }

}

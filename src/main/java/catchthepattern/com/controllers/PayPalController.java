package catchthepattern.com.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import catchthepattern.com.paypal.PayPalClient;
import catchthepattern.com.services.PatternService;
import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin
@RestController
public class PayPalController { 
    
    @Autowired
    private PayPalClient payPalClient;    

    @CrossOrigin(origins = {"http://localhost:4210", "https://www.catchthepattern.com", "https://catchthepattern.com", "https://api.catchthepattern.com"})
    @PostMapping(value = "/paypal/make/payment")
    public Map<String, Object> makePayment(@RequestParam("sum") String sum){
        return payPalClient.createPayment(sum);
    }
    
    /*
    @CrossOrigin(origins = {"http://localhost:4210", "https://www.catchthepattern.com", "https://api.catchthepattern.com"})
    @PostMapping(value = "/paypal/make/payment")
    public Map<String, Object> makePayment_new(@RequestBody Map<String, Object> payload){
        String username = (String) payload.get("username");
        String sum = (String) payload.get("sum");
        return payPalClient.createPayment_new(username, sum);
    }
    */

    @CrossOrigin(origins = {"http://localhost:4210", "https://www.catchthepattern.com", "https://catchthepattern.com", "https://api.catchthepattern.com"})
    @PostMapping(value = "/paypal/complete/payment")
    public ResponseEntity<?> completePayment(HttpServletRequest request, @RequestParam("paymentId") String paymentId, @RequestParam("payerId") String payerId){
        System.out.println("controller completePayment called");
        return payPalClient.completePayment(request);
    }
}

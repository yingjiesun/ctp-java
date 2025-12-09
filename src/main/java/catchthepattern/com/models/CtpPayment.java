package catchthepattern.com.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity(name = "payments")
public class CtpPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 
    
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name="userId", nullable=false)
    private User user;
    
    private String payer_email;
    private String payer_first_name;
    private String payer_last_name;
    private String payer_id;
    
    private String pay_id; 
    private double amount;
    private String currency; 
    private String create_time;
    private String update_time;
    
    private String payee_email;
    
    public CtpPayment() {}

    public CtpPayment(User user, String payer_email, String payer_first_name, String payer_last_name, String payer_id,
            String pay_id, double amount, String currency, String create_time, String update_time,
            String payee_email) {
        super();
        this.user = user;
        this.payer_email = payer_email;
        this.payer_first_name = payer_first_name;
        this.payer_last_name = payer_last_name;
        this.payer_id = payer_id;
        this.pay_id = pay_id;
        this.amount = amount;
        this.currency = currency;
        this.create_time = create_time;
        this.update_time = update_time;
        this.payee_email = payee_email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPayer_email() {
        return payer_email;
    }

    public void setPayer_email(String payer_email) {
        this.payer_email = payer_email;
    }

    public String getPayer_first_name() {
        return payer_first_name;
    }

    public void setPayer_first_name(String payer_first_name) {
        this.payer_first_name = payer_first_name;
    }

    public String getPayer_last_name() {
        return payer_last_name;
    }

    public void setPayer_last_name(String payer_last_name) {
        this.payer_last_name = payer_last_name;
    }

    public String getPayer_id() {
        return payer_id;
    }

    public void setPayer_id(String payer_id) {
        this.payer_id = payer_id;
    }


    public String getPayee_email() {
        return payee_email;
    }

    public void setPayee_email(String payee_email) {
        this.payee_email = payee_email;
    }

    public String getPay_id() {
        return pay_id;
    }

    public void setPay_id(String pay_id) {
        this.pay_id = pay_id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }
    
    
    
}

package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.repositories.*;
import com.stripe.model.*;
import com.stripe.model.BankAccount;
import com.stripe.net.RequestOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.nus.tt02backend.repositories.LocalRepository;
import com.nus.tt02backend.repositories.TouristRepository;
import com.nus.tt02backend.repositories.UserRepository;
import com.stripe.exception.StripeException;

import java.util.*;
import java.math.BigDecimal;

@Service
public class PaymentService {

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    VendorRepository vendorRepository;

    @Autowired
    TouristRepository touristRepository;

    @Autowired
    LocalRepository localRepository;

    @Autowired
    UserRepository userRepository;

    public String retrieveStripeId(String user, String field) {

        if (user.equals("TOURIST")) {
            return touristRepository.getStripeIdByEmail(field);
        } else if (user.equals("LOCAL")) {

            return localRepository.getStripeIdByEmail(field);
        }

        return "";
    }

    public PaymentMethod retrievePaymentMethod(String payment_method_id) throws StripeException {

        return PaymentMethod.retrieve(
                payment_method_id
        );
    }

    public String createStripeAccount(String account_type, Map<String,Object> parameters) {

        try {
            if (Objects.equals(account_type, "CUSTOMER")) {
                Customer customer = Customer.create(parameters);

                return customer.getId();
            } else if (Objects.equals(account_type, "VENDOR")) {
                Account account = Account.create(parameters);

                return account.getId();
            }

        } catch (Exception e) {
            // handle error
            return null;
        }
        return null;
    }


    public List<PaymentMethod> getPaymentMethods(String user_type, String tourist_email) throws StripeException {
        System.out.println(tourist_email);
        String tourist_stripe_id = retrieveStripeId(user_type,tourist_email);
        System.out.println(tourist_stripe_id);
        Map<String, Object> params = new HashMap<>();
        params.put("customer", tourist_stripe_id);
        params.put("type", "card");
        PaymentMethodCollection paymentMethods =
                PaymentMethod.list(params);

        return paymentMethods.getData();
    }


    public String addPaymentMethod(String user_type, String tourist_email,  String payment_method_id) throws StripeException {
        String tourist_stripe_id = retrieveStripeId(user_type, tourist_email);
        PaymentMethod paymentMethod = retrievePaymentMethod(payment_method_id);

        Map<String, Object> params = new HashMap<>();

        params.put("customer", tourist_stripe_id);

        PaymentMethod updatedPaymentMethod =
                paymentMethod.attach(params);

        return updatedPaymentMethod.getId();
    }

    public String deletePaymentMethod(String user_type, String tourist_email,  String payment_method_id
    ) throws StripeException {
        PaymentMethod paymentMethod = retrievePaymentMethod(payment_method_id);

        PaymentMethod updatedPaymentMethod =
                paymentMethod.detach();
        return updatedPaymentMethod.getId();
    }

    public String updatePaymentMethod(String payment_method_id, Integer exp_month, Integer exp_year) throws StripeException {
        PaymentMethod paymentMethod = retrievePaymentMethod(payment_method_id);

        Map<String, Object> card = new HashMap<>();
        card.put("exp_month", exp_month);
        card.put("exp_year", exp_year);
        Map<String, Object> params = new HashMap<>();
        params.put("card", card);

        PaymentMethod updatedPaymentMethod =
                paymentMethod.update(params);
        return updatedPaymentMethod.getId();
    }

    public BigDecimal getVendorTotalEarnings(Long vendorId) throws BadRequestException {
        BigDecimal sum = new BigDecimal(0);
        Optional<Vendor> vendorOptional = vendorRepository.findById(vendorId);

        if (vendorOptional.isPresent()) {
            Vendor vendor = vendorOptional.get();
            vendor.setVendor_staff_list(null);

            // fetch attractions
            List<Attraction> attractionList = vendor.getAttraction_list();
            for (Attraction a : attractionList) {
                Double tempSum = paymentRepository.retrieveSumOfBookingByAttractionId(a.getAttraction_id());
                sum = sum.add(new BigDecimal(tempSum));
            }

            List<Telecom> telecomList = vendor.getTelecom_list();
            for (Telecom t : telecomList) {
                Double tempSum = paymentRepository.retrieveSumOfBookingByTelecomId(t.getTelecom_id());
                sum = sum.add(new BigDecimal(tempSum));
            }

            List<Deal> dealList = vendor.getDeals_list();
            for (Deal d : dealList) {
                Double tempSum = paymentRepository.retrieveSumOfBookingByDealId(d.getDeal_id());
                sum = sum.add(new BigDecimal(tempSum));
            }

            List<Accommodation> accommodationList = vendor.getAccommodation_list();
            List<Room> roomList = new ArrayList<>();
            for (Accommodation a : accommodationList) {
                List<Room> tempRoom = a.getRoom_list();
                roomList.addAll(tempRoom);
            }

            for (Room r : roomList) {
                Double tempSum = paymentRepository.retrieveSumOfBookingByRoomId(r.getRoom_id());
                sum = sum.add(new BigDecimal(tempSum));
            }

            sum = sum.multiply(new BigDecimal(0.9)); // 10% commission removal
            return sum;
        } else {
            throw new BadRequestException("Vendor not found!");
        }
    }

    public BigDecimal getTourTotalEarningForLocal(Long localId) {
        Double sum = paymentRepository.retrieveTourEarningsByLocalId(localId);
        BigDecimal totalEarned = new BigDecimal(sum * 0.9);
        return totalEarned;
    }

    public String addBankAccount(Long userId, String token) throws NotFoundException, StripeException {

        Optional<Local> localOptional = localRepository.findById(userId);

        if (localOptional.isPresent()) {
            Local local = localOptional.get();

            String stripe_business_id = local.getStripe_business_id();

            Account account =
                    Account.retrieve(stripe_business_id);

            Map<String, Object> params = new HashMap<>();
            params.put(
                    "external_account",
                    token
            );

            com.stripe.model.BankAccount bankAccount =
                    (BankAccount) account
                            .getExternalAccounts()
                            .create(params);

            return bankAccount.getId();

        } else {
            throw new NotFoundException("Local not found!");
        }

    }

    public String deleteBankAccount(Long userId, String bank_account_id) throws NotFoundException, StripeException {

        Optional<Local> localOptional = localRepository.findById(userId);

        if (localOptional.isPresent()) {
            Local local = localOptional.get();

            String stripe_business_id = local.getStripe_business_id();

            Account account =
                    Account.retrieve(stripe_business_id);

            BankAccount bankAccount =
                    (BankAccount) account.getExternalAccounts().retrieve(
                            bank_account_id
                    );

            BankAccount deletedBankAccount =
                    bankAccount.delete();

            return deletedBankAccount.getId();

        } else {
            throw new NotFoundException("Local not found!");
        }

    }

    public List<ExternalAccount> getBankAccounts(Long userId) throws NotFoundException, StripeException {
        Optional<Local> localOptional = localRepository.findById(userId);

        if (localOptional.isPresent()) {
            Local local = localOptional.get();

            String stripe_business_id = local.getStripe_business_id();

            Account account =
                    Account.retrieve(stripe_business_id);

            Map<String, Object> params = new HashMap<>();
            params.put("object", "bank_account");
            params.put("limit", 10);

            ExternalAccountCollection bankAccountsCollection =
                    account.getExternalAccounts().list(params);

            List<ExternalAccount> bankAccounts = bankAccountsCollection.getData();

            return bankAccounts;

        } else {
            throw new NotFoundException("Local not found!");
        }
    }

    public String withdrawWallet(Long userId, String bank_account_id, BigDecimal amount) throws StripeException, NotFoundException {

        Optional<Local> localOptional = localRepository.findById(userId);

        if (localOptional.isPresent()) {
            Local local = localOptional.get();

            String stripe_business_id = local.getStripe_business_id();

            Map<String, Object> params = new HashMap<>();
            params.put("amount", amount);
            params.put("currency", "sgd");
            params.put("destination", bank_account_id);

            RequestOptions requestOptions =
                    RequestOptions.builder().setStripeAccount(stripe_business_id).build();


            Payout payout = Payout.create(params, requestOptions);


            local.setWallet_balance(local.getWallet_balance().subtract(amount));

            localRepository.save(local);

            return payout.getId();

        } else {
            throw new NotFoundException("Vendor Staff not found!");
        }


    }

    public String topUpWallet(Long userId, BigDecimal amount) throws StripeException, NotFoundException {

        Optional<Local> localOptional = localRepository.findById(userId);

        if (localOptional.isPresent()) {
            Local local = localOptional.get();

            String stripe_business_id = local.getStripe_business_id();

            Map<String, Object> params = new HashMap<>();
            params.put("amount", amount);
            params.put("currency", "sgd");
            params.put("source", stripe_business_id );
            params.put(
                    "description",
                    "Withdrawal"
            );


            Charge charge = Charge.create(params);


            local.setWallet_balance(local.getWallet_balance().subtract(amount));

            localRepository.save(local);

            return charge.getId();

        } else {
            throw new NotFoundException("Vendor Staff not found!");
        }


    }
}

package com.deskforge.order.dto;

import com.deskforge.order.ShippingAddress;

public record AddressResponse(String recipientName, String zipCode, String street, String number,
                              String complement, String district, String city, String state) {

    public static AddressResponse from(ShippingAddress a) {
        return new AddressResponse(a.getRecipientName(), a.getZipCode(), a.getStreet(), a.getNumber(),
                a.getComplement(), a.getDistrict(), a.getCity(), a.getState());
    }
}

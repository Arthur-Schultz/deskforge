package com.deskforge.order.dto;

import com.deskforge.demo.PersonalDataMask;
import com.deskforge.order.ShippingAddress;

public record AddressResponse(String recipientName, String zipCode, String street, String number,
                              String complement, String district, String city, String state) {

    public static AddressResponse from(ShippingAddress a) {
        return new AddressResponse(a.getRecipientName(), a.getZipCode(), a.getStreet(), a.getNumber(),
                a.getComplement(), a.getDistrict(), a.getCity(), a.getState());
    }

    public AddressResponse masked() {
        return new AddressResponse(PersonalDataMask.name(recipientName), zipCode.substring(0, 2) + "•••-•••",
                PersonalDataMask.hidden(street), PersonalDataMask.hidden(number), PersonalDataMask.hidden(complement),
                district, city, state);
    }
}

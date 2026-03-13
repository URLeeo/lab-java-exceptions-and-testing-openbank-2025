package org.example.springbrestapi_lab;

import org.example.springbrestapi_lab.controller.CustomerController;
import org.example.springbrestapi_lab.exception.ResourceNotFoundException;
import org.example.springbrestapi_lab.model.Customer;
import org.example.springbrestapi_lab.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    private Customer createCustomer(String name, String email, int age, String address) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setEmail(email);
        customer.setAge(age);
        customer.setAddress(address);
        return customer;
    }

    @Test
    void addCustomer_ShouldAddAndReturnCustomer() throws Exception {
        Customer customer = createCustomer("Aslan", "aslan@gmail.com", 22, "Baku");

        given(customerService.addCustomer(any(Customer.class))).willReturn(customer);

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Aslan"))
                .andExpect(jsonPath("$.email").value("aslan@gmail.com"))
                .andExpect(jsonPath("$.age").value(22))
                .andExpect(jsonPath("$.address").value("Baku"));
    }

    @Test
    void getAllCustomers_ShouldReturnAllCustomers() throws Exception {
        List<Customer> customers = List.of(
                createCustomer("Aslan", "aslan@gmail.com", 22, "Baku"),
                createCustomer("Ali", "ali@gmail.com", 25, "Ganja")
        );

        given(customerService.getAllCustomers()).willReturn(customers);

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Aslan"))
                .andExpect(jsonPath("$[1].name").value("Ali"));
    }

    @Test
    void getCustomerByEmail_ShouldReturnCustomer_WhenEmailExists() throws Exception {
        Customer customer = createCustomer("Aslan", "aslan@gmail.com", 22, "Baku");

        given(customerService.getCustomerByEmail("aslan@gmail.com")).willReturn(customer);

        mockMvc.perform(get("/customers/aslan@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Aslan"))
                .andExpect(jsonPath("$.email").value("aslan@gmail.com"));
    }

    @Test
    void getCustomerByEmail_ShouldThrowException_WhenEmailDoesNotExist() throws Exception {
        given(customerService.getCustomerByEmail("notfound@gmail.com"))
                .willThrow(new ResourceNotFoundException("Customer not found"));

        mockMvc.perform(get("/customers/notfound@gmail.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCustomer_ShouldUpdateAndReturnCustomer_WhenCustomerExists() throws Exception {
        Customer updatedCustomer = createCustomer("Aslan Mammadzada", "aslannew@gmail.com", 23, "Sumqayit");

        given(customerService.updateCustomer(eq("aslan@gmail.com"), any(Customer.class)))
                .willReturn(updatedCustomer);

        mockMvc.perform(put("/customers/aslan@gmail.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCustomer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Aslan Mammadzada"))
                .andExpect(jsonPath("$.email").value("aslannew@gmail.com"))
                .andExpect(jsonPath("$.age").value(23))
                .andExpect(jsonPath("$.address").value("Sumqayit"));
    }

    @Test
    void updateCustomer_ShouldThrowException_WhenCustomerDoesNotExist() throws Exception {
        Customer updatedCustomer = createCustomer("Aslan", "aslan@gmail.com", 22, "Baku");

        given(customerService.updateCustomer(eq("missing@gmail.com"), any(Customer.class)))
                .willThrow(new ResourceNotFoundException("Customer not found"));

        mockMvc.perform(put("/customers/missing@gmail.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCustomer)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCustomer_ShouldRemoveCustomer_WhenCustomerExists() throws Exception {
        willDoNothing().given(customerService).deleteCustomer("aslan@gmail.com");

        mockMvc.perform(delete("/customers/aslan@gmail.com"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCustomer_ShouldThrowException_WhenCustomerDoesNotExist() throws Exception {
        willThrow(new ResourceNotFoundException("Customer not found"))
                .given(customerService).deleteCustomer("missing@gmail.com");

        mockMvc.perform(delete("/customers/missing@gmail.com"))
                .andExpect(status().isNotFound());
    }
}
package com.henriquericcio.contacts.adapters.api;

import com.henriquericcio.contacts.adapters.api.query.Contact;
import com.henriquericcio.contacts.adapters.api.query.ContactQuery;
import com.henriquericcio.contacts.inbound.CreateContactUseCase;
import com.henriquericcio.contacts.inbound.DeleteContactUseCase;
import com.henriquericcio.contacts.inbound.UpdateContactUseCase;
import com.henriquericcio.contacts.inbound.common.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/contacts")
@RequiredArgsConstructor
public class ContactController {
    private final CreateContactUseCase createContactUseCase;
    private final DeleteContactUseCase deleteContactUseCase;
    private final UpdateContactUseCase updateContactUseCase;
    private final ContactQuery contactQuery;


    @PostMapping
    public ResponseEntity<?> addContact(@RequestBody ContactPayload contact) {
        log.info("Receiving payload {}", contact);

        val command = new CreateContactUseCase.CreateContactCommand(contact.getFirstName(), contact.getLastName(), contact.getPhoneNumber());
        val contactId = createContactUseCase.create(command);
        return ResponseEntity.created(URI.create("/" + contactId.getValue())).build();
    }

    @SneakyThrows
    @PutMapping(path = "{id}")
    public ResponseEntity<?> updateContact(@PathVariable("id") String id, @RequestBody ContactPayload contact) {

        val command = new UpdateContactUseCase.UpdateContactCommand(id, contact.getFirstName(), contact.getLastName(), contact.getPhoneNumber());

        try {
            updateContactUseCase.update(command);
        } catch (NotFoundException e) {
            throw new com.henriquericcio.contacts.adapters.api.NotFoundException();
        }

        return ResponseEntity.noContent().build();
    }

    @SneakyThrows
    @DeleteMapping(path = "{id}")
    public ResponseEntity<?> deleteContact(@PathVariable("id") String id) {

        try {
            deleteContactUseCase.delete(new DeleteContactUseCase.DeleteContactCommand(id));
        } catch (NotFoundException e) {
            throw new com.henriquericcio.contacts.adapters.api.NotFoundException();
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public Iterable<Contact> getAllContacts() {
        return contactQuery.findAll();
    }

    @SneakyThrows
    @GetMapping(path = "{id}")
    public Contact getContactById(@PathVariable("id") String id) {
        val contact = contactQuery.findById(id);
        if (contact.isEmpty())
            throw new com.henriquericcio.contacts.adapters.api.NotFoundException();

        return contact.get();
    }
}
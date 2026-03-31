package com.magicalAliance.service;

import com.magicalAliance.entity.Contacto;
import com.magicalAliance.repository.ContactoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactoServiceImpl implements IContactoService {

    @Autowired
    private ContactoRepository contactoRepository;

    @Override
    @Transactional
    public void guardar(Contacto contacto) {
        contactoRepository.save(contacto);
    }
}
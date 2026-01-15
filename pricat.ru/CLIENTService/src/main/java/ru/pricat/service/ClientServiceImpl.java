package ru.pricat.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pricat.exception.EmailNotUniqueException;
import ru.pricat.exception.ServiceUnavailableException;
import ru.pricat.exception.UserNotFoundException;
import ru.pricat.exception.UsernameNotUniqueException;
import ru.pricat.model.Client;
import ru.pricat.model.dto.request.ProfileCreateRequestDto;
import ru.pricat.model.dto.request.ProfilePatchRequestDto;
import ru.pricat.model.dto.request.ProfileUpdateRequestDto;
import ru.pricat.model.dto.response.AdminProfileDto;
import ru.pricat.model.dto.response.ProfileResponseDto;
import ru.pricat.model.dto.response.mapper.ClientMapper;
import ru.pricat.repository.ClientRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    private final ClientMapper clientMapper;

    @Override
    @Transactional
    public Client createClientProfile(@NonNull ProfileCreateRequestDto request) {
        log.info("Creating profile for user: {}", request.username());
        try {
            if (clientRepository.existsByUsername(request.username())) {
                log.warn("Username already exists: {}", request.username());
                throw new UsernameNotUniqueException("Username already exists: " + request.email());
            }
            if (clientRepository.existsByEmail(request.email())) {
                log.warn("Email already exists: {}", request.email());
                throw new EmailNotUniqueException("Email already exists: " + request.email());
            }
            Client client = new Client(request.username(), request.email(), request.name());
            client.addRole(String.valueOf(Client.Role.USER));
            if (request.name() == null || request.name().isBlank()) {
                client.setName(request.username());
            }
            Client savedClient = clientRepository.save(client);
            log.info("Successfully created profile for user: {}", request.username());
            return savedClient;
        } catch (DataAccessException e) {
            log.error("Database error while creating profile for user: {}", request.username(), e);
            throw new ServiceUnavailableException("Service temporarily unavailable. Please try again later.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isClientEmailUnique(String email) {
        log.info("Checking email uniqueness for: {}", email);
        try {
            boolean exists = clientRepository.existsByEmail(email);
            log.info("Email '{}' is unique: {}", email, !exists);
            return !exists;
        } catch (DataAccessException e) {
            log.error("Database error while checking email uniqueness: {}", email, e);
            throw new ServiceUnavailableException("Service temporarily unavailable. Please try again later.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminProfileDto> getAllClients(@NonNull Pageable pageable) {
        log.info("Getting all clients with pagination: page {}, size {}", pageable.getPageNumber(), pageable.getPageSize());
        try {
            Page<Client> clientsPage = clientRepository.findAll(pageable);
            return clientsPage.map(clientMapper::toAdminProfileDto);
        } catch (DataAccessException e) {
            log.error("Database error while getting all clients", e);
            throw new ServiceUnavailableException("Service temporarily unavailable");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Client getClientProfileByEmail(String email) {
        log.info("Getting client by email: {}", email);
        try {
            return clientRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("Client not found with email: {}", email);
                        return new UserNotFoundException("Client not found with email: " + email);
                    });
        } catch (DataAccessException e) {
            log.error("Database error while get client profile by email: {}", email, e);
            throw new ServiceUnavailableException("Service temporarily unavailable. Please try again later.");
        }

    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsClientProfileByUsername(String username) {
        log.info("Checking if client profile exists by username: {}", username);
        try {
            return clientRepository.existsByUsername(username);
        } catch (DataAccessException e) {
            log.error("Database error while checking if client profile exists by username: {}", username, e);
            throw new ServiceUnavailableException("Service temporarily unavailable. Please try again later.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsClientProfileByEmail(String email) {
        log.info("Checking if client profile exists by email: {}", email);
        try {
            return clientRepository.existsByEmail(email);
        } catch (DataAccessException e) {
            log.error("Database error while checking if client profile exists by email: {}", email, e);
            throw new ServiceUnavailableException("Service temporarily unavailable. Please try again later.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AdminProfileDto getCurrentUserProfile(String username) {
        log.info("Getting current user profile for: {}", username);
        Client client = getClientProfileByUsername(username);
        if (!client.hasRole("ADMIN")) {
            return AdminProfileDto.builder()
                    .id(client.getId())
                    .username(client.getUsername())
                    .email(client.getEmail())
                    .name(client.getName())
                    .roles(String.join(",", client.getRoles()))
                    .companyName(client.getCompanyName())
                    .mobilePhone(client.getMobilePhone())
                    .avatarUrl(client.getAvatarUrl())
                    .isSupplier(client.isSupplier())
                    .companyFolder(client.getCompanyFolder())
                    .pricelistObtainingWay(client.getPricelistObtainingWay())
                    .pricelistFormat(client.getPricelistFormat())
                    .createdAt(client.getCreatedAt())
                    .updatedAt(client.getUpdatedAt())
                    .build();
        }
        return clientMapper.toAdminProfileDto(client);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminProfileDto getUserProfile(String username) {
        log.info("Getting user profile for: {}", username);
        Client client = getClientProfileByUsername(username);
        return clientMapper.toAdminProfileDto(client);
    }

    @Override
    @Transactional
    public ProfileResponseDto updateCurrentUserProfile(String username, ProfileUpdateRequestDto request) {
        log.info("Updating current user profile for: {}", username);
        Client client = getClientProfileByUsername(username);
        if (!client.getEmail().equals(request.email()) && !isClientEmailUnique(request.email())) {
            throw new EmailNotUniqueException("Email already exists: " + request.email());
        }
        updateClientFromRequest(client, request);
        try {
            Client updatedClient = clientRepository.save(client);
            log.info("Successfully updated profile for: {}", username);
            return clientMapper.toProfileResponseDto(updatedClient);
        } catch (DataAccessException e) {
            log.error("Database error while updating current user profile for: {}", username, e);
            throw new ServiceUnavailableException("Service temporarily unavailable. Please try again later.");
        }

    }

    @Override
    @Transactional
    public ProfileResponseDto updateUserProfile(String username, @NonNull ProfileUpdateRequestDto request) {
        log.info("Admin updating user profile for: {}", username);
        Client client = getClientProfileByUsername(username);
        if (!client.getEmail().equals(request.email()) && !isClientEmailUnique(request.email())) {
            throw new EmailNotUniqueException("Email already exists: " + request.email());
        }
        updateClientFromRequest(client, request);
        try {
            Client updatedClient = clientRepository.save(client);
            log.info("Admin successfully updated profile for: {}", username);
            return clientMapper.toProfileResponseDto(updatedClient);
        } catch (DataAccessException e) {
            log.error("Database error while admin updating current user profile for: {}", username, e);
            throw new ServiceUnavailableException("Service temporarily unavailable. Please try again later.");
        }
    }

    @Override
    @Transactional
    public ProfileResponseDto patchCurrentUserProfile(String username, @NonNull ProfilePatchRequestDto request) {
        log.info("Patching current user profile for: {}", username);
        Client client = getClientProfileByUsername(username);
        if (request.email() != null &&
            !client.getEmail().equals(request.email()) &&
            !isClientEmailUnique(request.email())) {
            throw new EmailNotUniqueException("Email already exists: " + request.email());
        }
        patchClientFromRequest(client, request);
        try {
            Client updatedClient = clientRepository.save(client);
            log.info("Successfully patched profile for: {}", username);
            return clientMapper.toProfileResponseDto(updatedClient);
        } catch (DataAccessException e) {
            log.error("Database error while patching current user profile for: {}", username, e);
            throw new ServiceUnavailableException("Service temporarily unavailable. Please try again later.");
        }
    }

    @Override
    @Transactional
    public void deleteProfile(String username) {
        log.info("Deleting profile for: {}", username);
        try {
            clientRepository.delete(getClientProfileByUsername(username));
            log.info("Successfully deleted profile for: {}", username);
        } catch(DataAccessException e) {
            log.error("Database error while Deleting profile for: {}", username, e);
            throw new ServiceUnavailableException("Service temporarily unavailable. Please try again later.");
        }
    }

    private Client getClientProfileByUsername(String username) {
        log.info("Getting client by username: {}", username);
        try {
            return clientRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.warn("Client not found with username: {}", username);
                        return new UserNotFoundException("Client not found with username: " + username);
                    });
        } catch (DataAccessException e) {
            log.error("Database error while get client profile by username: {}", username, e);
            throw new ServiceUnavailableException("Service temporarily unavailable. Please try again later.");
        }
    }

    private void updateClientFromRequest(@NonNull Client client, @NonNull ProfileUpdateRequestDto request) {
        client.setName(request.name());
        client.setEmail(request.email());
        client.setCompanyName(request.companyName());
        client.setMobilePhone(request.mobilePhone());
        client.setAvatarUrl(request.avatarUrl());
        client.setIsSupplier(request.isSupplier());
        client.setCompanyFolder(request.companyFolder());
        client.setPricelistObtainingWay(request.pricelistObtainingWay());
        client.setPricelistFormat(request.pricelistFormat());
    }

    private void patchClientFromRequest(@NonNull Client client, @NonNull ProfilePatchRequestDto request) {
        if (request.name() != null) {
            client.setName(request.name());
        }
        if (request.email() != null) {
            client.setEmail(request.email());
        }
        if (request.companyName() != null) {
            client.setCompanyName(request.companyName());
        }
        if (request.mobilePhone() != null) {
            client.setMobilePhone(request.mobilePhone());
        }
        client.setIsSupplier(request.isSupplier());
        client.setPricelistObtainingWay(request.pricelistObtainingWay());
        client.setPricelistFormat(request.pricelistFormat());
    }
}

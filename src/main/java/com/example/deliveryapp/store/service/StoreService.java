package com.example.deliveryapp.store.service;

import com.example.deliveryapp.menu.repository.MenuRepository;
import com.example.deliveryapp.order.repository.OrderRepository;
import com.example.deliveryapp.review.repository.ReviewRepository;
import com.example.deliveryapp.store.dto.request.StoreCreateRequestDto;
import com.example.deliveryapp.store.dto.request.StoreUpdateRequestDto;
import com.example.deliveryapp.store.dto.response.StoreResponseDto;
import com.example.deliveryapp.store.entity.Store;
import com.example.deliveryapp.store.repository.StoreRepository;
import com.example.deliveryapp.user.entity.User;
import com.example.deliveryapp.user.enums.UserRole;
import com.example.deliveryapp.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;
    private final ReviewRepository reviewRepository;
    // 가게 생성 메서드
    public StoreResponseDto createStore(StoreCreateRequestDto storeCreateRequestDto, String username) {
        // 사장님 권한 확인
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 권한이 사장님(OWNER)인지 확인
        if (user.getUserRole() != UserRole.ROLE_OWNER) {
            throw new IllegalStateException("사장님 권한이 필요합니다.");
        }

        // 가게가 이미 3개 이상인 경우 예외 처리
        if (storeRepository.countByOwnerId(user.getId()) >= 3) {
            throw new IllegalStateException("가게는 최대 3개까지 운영할 수 있습니다.");
        }

        // Store 객체 생성
        Store store = new Store();
        store.setBusinessName(storeCreateRequestDto.getBusinessName());
        store.setCategory(storeCreateRequestDto.getCategory());
        store.setOpeningTime(storeCreateRequestDto.getOpeningTime());
        store.setClosingTime(storeCreateRequestDto.getClosingTime());
        store.setMinOrderPrice(storeCreateRequestDto.getMinOrderPrice());
        store.setAddress(storeCreateRequestDto.getAddress());
        store.setOwner(user);

        // 저장
        Store savedStore = storeRepository.save(store);

        // 생성된 가게 정보를 StoreResponseDto로 반환
        return new StoreResponseDto(savedStore);
    }

    //가게명으로 검색
    public List<StoreResponseDto> searchStores(String keyword) {
        List<Store> stores = storeRepository.findByBusinessNameContaining(keyword);

        // Store 객체들을 StoreResponseDto 리스트로 변환하여 반환
        return stores.stream()
                .map(StoreResponseDto::new)
                .collect(Collectors.toList());
    }


    // 가게 단건 조회
    public StoreResponseDto findStoreById(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 가게를 찾을 수 없습니다."));

        return new StoreResponseDto(store); // Store 객체를 StoreResponseDto로 변환하여 반환
    }

    //가게 정보 수정
    public StoreResponseDto updateStore(Long storeId, StoreUpdateRequestDto requestDto, User storeOwner) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 가게를 찾을 수 없습니다."));

        // 가게 주인인지 확인
        if (!store.getOwner().equals(storeOwner)) {
            throw new IllegalArgumentException("본인의 가게만 수정할 수 있습니다.");
        }

        // 모든 필드를 업데이트
        store.setBusinessName(requestDto.getBusinessName());
        store.setCategory(requestDto.getCategory());
        store.setOpeningTime(requestDto.getOpeningTime());
        store.setClosingTime(requestDto.getClosingTime());
        store.setMinOrderPrice(requestDto.getMinOrderPrice());
        store.setAddress(requestDto.getAddress());

        // 가게 정보만 수정
        Store updatedStore = storeRepository.save(store); // 변경된 정보 저장

        // 수정된 가게 정보를 StoreResponseDto로 반환
        return new StoreResponseDto(updatedStore);
    }



    // 가게 삭제 메서드
    @Transactional
    public void deleteStore(Long storeId, User storeOwner) {
        // 삭제하려는 가게 찾기
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다."));

        // 가게가 해당 사용자 소유인지 확인
        if (!store.getOwner().equals(storeOwner)) {
            throw new IllegalStateException("자신의 가게만 삭제할 수 있습니다.");
        }
        //각각의 repository삭제
        reviewRepository.deleteByStore(store);
        orderRepository.deleteByStore(store);
        menuRepository.deleteByStore(store);

        // 가게 삭제
        storeRepository.delete(store);
    }
}

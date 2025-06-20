package teo.springjwt.order;

public enum OrderStatus {
  PENDING,          // 주문 접수됨
  PAID,             // 결제 완료
  PROCESSING,       // 주문 처리 중 (포장 등)
  SHIPPED,          // 배송 시작됨
  DELIVERED,        // 배송 완료
  CANCELLED,        // 주문 취소됨
  RETURN_REQUESTED, // 반품 요청됨
  RETURNED          // 반품 완료
}

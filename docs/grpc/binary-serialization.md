# 텍스트 직렬화 vs 바이너리 직렬화

gRPC 의 장점중 하나로 데이터를 `Protocol Buffers`를 사용한 바이너리 직렬화입니다. 

이를 통해 네트워크 전송 시 데이터 크기를 크게 줄여 오버헤드를 감소시킬 수 있습니다.

실제로 기존 JSON 의 직렬화와 바이너리 직렬화가 얼마나 데이터의 크기를 줄여주는지 확인해보겠습니다.

## 직렬화 방식 비교

### JSON (텍스트 직렬화)
- 사람이 읽을 수 있는 형식
- 필드명이 매번 전송됨
- 숫자도 문자열로 표현
- 구조를 위한 문자({}, "", :) 필요

### Protocol Buffers (바이너리 직렬화)
- 바이너리 형식
- 필드 번호만 전송 (필드명 불필요)
- 효율적인 varint 인코딩
- 최소한의 구조 정보

### 예시 코드

실제 직렬화시 어떻게 표기되는지 확인해 보고 테스트 코드를 통해서 실제 바이터리 직렬화가 얼마나 크기를 감소시키는지 확인해보겠습니다

```kotlin
@Test
fun `텍스트 vs 바이너리 직렬화 비교`() {
    val json = PointUseRequest(
        userId = 1L,
        amount = 1000L,
    )

    val protobuf = GrpcPointUseRequest.newBuilder()
        .setUserId(1L)
        .setAmount(1000L)
        .build()

    // JSON 직렬화
    val jsonBytes = JsonUtil.encodeToJson(json).toByteArray()
    println("JSON 크기: ${jsonBytes.size} bytes")
    println("JSON 내용: ${String(jsonBytes)}")
    println()


    // Protobuf 직렬화
    val protobufBytes = protobuf.toByteArray()
    println("Protobuf 크기: ${protobufBytes.size} bytes")
    println("Protobuf 내용(hex): ${protobufBytes.joinToString(" ") { "%02X".format(it) }}")
    println()

    // 비교
    val reduction = ((jsonBytes.size - protobufBytes.size) * 100.0 / jsonBytes.size)
    println("크기 절감: ${"%.1f".format(reduction)}%")
}
```

```text
JSON 크기: 39 bytes
JSON 내용: {
    "userId": 1,
    "amount": 1000
}

Protobuf 크기: 5 bytes
Protobuf 내용(hex): 08 01 10 E8 07

크기 절감: 87.2%
```

gRPC 의 바이너리 직렬화를 사용하면 JSON 대비 약 87% 정도의 크기 절감 효과를 볼 수 있는 것을 확인할 수 있었습니다

테스트는 정말 간단하게 2개의 데이터만 전송하는 테스트였지만 실제 서비스에서는 더 많은 데이터를 전송하게 되고, 그에 따라 절감되는 크기 또한 더 커지게 되므로 gRPC 의 바이너리 직렬화는 네트워크 오버헤드를 줄이는데 큰 도움이 되는것을 확인할 수 있었습니다.


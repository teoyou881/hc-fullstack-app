/**
 * 두 개의 인자를 숫자로 변환한 뒤, 두 숫자가 같은지 비교하는 함수입니다.
 * 숫자로 변환할 수 없는 인자는 NaN으로 처리되며, NaN은 NaN과 같지 않습니다.
 *
 * @param {any} arg1 - 첫 번째 인자
 * @param {any} arg2 - 두 번째 인자
 * @returns {boolean} 두 인자를 숫자로 변환한 값이 같으면 true, 아니면 false
 */
export const isEqualAsNumber = (arg1, arg2) => {
  const num1 = Number(arg1);
  const num2 = Number(arg2);

  // NaN은 자기 자신과 비교해도 false를 반환하므로, isNaN 체크가 필요합니다.
  if (isNaN(num1) || isNaN(num2)) {
    return false; // 둘 중 하나라도 숫자로 변환할 수 없으면 (유효한 숫자가 아니면) 같다고 보지 않습니다.
  }

  return num1 === num2; // 두 숫자가 같은지 비교
};
function* foo() {
  yield 19
  yield 7
  yield 11
  return
  yield 5
}

function* fibonacci() {
    let prev = 0;
    let next = 1;
    while (true) {
        yield prev;
        const aux = prev + next;
        prev = next;
        next = aux;
    }
}
const iter = foo()
let item = iter.next()
console.log(item.value)

let i = 0;

for (const x of fibonacci()) {
    console.log(x);
    if (++i === 10) break;
}

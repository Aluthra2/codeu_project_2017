// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package codeu.chat.util.store;

import java.util.Comparator;
import java.util.Iterator;

final class LinkIterator<KEY, VALUE> implements Iterator<VALUE> {

  private final Comparator<KEY> comparator;
  private final StoreLink<KEY, VALUE> last;

  private StoreLink<KEY, VALUE> current;

  public LinkIterator(Comparator<KEY> comparator, StoreLink<KEY, VALUE> first, StoreLink<KEY, VALUE> last) {
    this.comparator = comparator;
    this.last = last;
    this.current = first;
  }

  @Override
  public boolean hasNext() {
    return current != null && comparator.compare(current.key, last.key) <= 0;
  }

  @Override
  public VALUE next() {
    final VALUE value = current.value;
    current = current.next;
    return value;
  }

  @Override
  public void remove() {
    //TODO

  }
}

require 'fileutils'

module FileUpdateCheckerSharedTests
  extend ActiveSupport::Testing::Declarative
  include FileUtils

  def tmpdir
    @tmpdir ||= Dir.mktmpdir(nil, __dir__)
  end

  def tmpfile(name)
    "#{tmpdir}/#{name}"
  end

  def tmpfiles
    @tmpfiles ||= %w(foo.rb bar.rb baz.rb).map { |f| tmpfile(f) }
  end

  def teardown
    FileUtils.rm_rf(@tmpdir) if defined? @tmpdir
  end

  test 'should not execute the block if no paths are given' do
    silence_warnings { require 'listen' }
    i = 0

    checker = new_checker { i += 1 }

    assert !checker.execute_if_updated
    assert_equal 0, i
  end

  test 'should not execute the block if no files change' do
    i = 0

    FileUtils.touch(tmpfiles)

    checker = new_checker(tmpfiles) { i += 1 }

    assert !checker.execute_if_updated
    assert_equal 0, i
  end

  test 'should execute the block once when files are created' do
    i = 0

    checker = new_checker(tmpfiles) { i += 1 }

    touch(tmpfiles)

    assert checker.execute_if_updated
    assert_equal 1, i
  end

  test 'should execute the block once when files are modified' do
    i = 0

    FileUtils.touch(tmpfiles)

    checker = new_checker(tmpfiles) { i += 1 }

    touch(tmpfiles)

    assert checker.execute_if_updated
    assert_equal 1, i
  end

  test 'should execute the block once when files are deleted' do
    i = 0

    FileUtils.touch(tmpfiles)

    checker = new_checker(tmpfiles) { i += 1 }

    rm_f(tmpfiles)

    assert checker.execute_if_updated
    assert_equal 1, i
  end


  test 'updated should become true when watched files are created' do
    i = 0

    checker = new_checker(tmpfiles) { i += 1 }
    assert !checker.updated?

    touch(tmpfiles)

    assert checker.updated?
  end


  test 'updated should become true when watched files are modified' do
    i = 0

    FileUtils.touch(tmpfiles)

    checker = new_checker(tmpfiles) { i += 1 }
    assert !checker.updated?

    touch(tmpfiles)

    assert checker.updated?
  end

  test 'updated should become true when watched files are deleted' do
    i = 0

    FileUtils.touch(tmpfiles)

    checker = new_checker(tmpfiles) { i += 1 }
    assert !checker.updated?

    rm_f(tmpfiles)

    assert checker.updated?
  end

  test 'should be robust to handle files with wrong modified time' do
    i = 0

    FileUtils.touch(tmpfiles)

    now  = Time.now
    time = Time.mktime(now.year + 1, now.month, now.day) # wrong mtime from the future
    File.utime(time, time, tmpfiles[0])

    checker = new_checker(tmpfiles) { i += 1 }

    touch(tmpfiles[1..-1])

    assert checker.execute_if_updated
    assert_equal 1, i
  end

  test 'should cache updated result until execute' do
    i = 0

    checker = new_checker(tmpfiles) { i += 1 }
    assert !checker.updated?

    touch(tmpfiles)

    assert checker.updated?
    checker.execute
    assert !checker.updated?
  end

  test 'should execute the block if files change in a watched directory one extension' do
    i = 0

    checker = new_checker([], tmpdir => :rb) { i += 1 }

    touch(tmpfile('foo.rb'))

    assert checker.execute_if_updated
    assert_equal 1, i
  end

  test 'should execute the block if files change in a watched directory several extensions' do
    i = 0

    checker = new_checker([], tmpdir => [:rb, :txt]) { i += 1 }

    touch(tmpfile('foo.rb'))

    assert checker.execute_if_updated
    assert_equal 1, i

    touch(tmpfile('foo.txt'))

    assert checker.execute_if_updated
    assert_equal 2, i
  end

  test 'should not execute the block if the file extension is not watched' do
    i = 0

    checker = new_checker([], tmpdir => :txt) { i += 1 }

    touch(tmpfile('foo.rb'))

    assert !checker.execute_if_updated
    assert_equal 0, i
  end

  test 'does not assume files exist on instantiation' do
    i = 0

    non_existing = tmpfile('non_existing.rb')
    checker = new_checker([non_existing]) { i += 1 }

    touch(non_existing)

    assert checker.execute_if_updated
    assert_equal 1, i
  end

  test 'detects files in new subdirectories' do
    i = 0

    checker = new_checker([], tmpdir => :rb) { i += 1 }

    subdir = tmpfile('subdir')
    mkdir(subdir)
    wait

    assert !checker.execute_if_updated
    assert_equal 0, i

    touch("#{subdir}/nested.rb")

    assert checker.execute_if_updated
    assert_equal 1, i
  end

  test 'looked up extensions are inherited in subdirectories not listening to them' do
    i = 0

    subdir = tmpfile('subdir')
    mkdir(subdir)

    checker = new_checker([], tmpdir => :rb, subdir => :txt) { i += 1 }

    touch(tmpfile('new.txt'))

    assert !checker.execute_if_updated
    assert_equal 0, i

    # subdir does not look for Ruby files, but its parent tmpdir does.
    touch("#{subdir}/nested.rb")

    assert checker.execute_if_updated
    assert_equal 1, i

    touch("#{subdir}/nested.txt")

    assert checker.execute_if_updated
    assert_equal 2, i
  end
end

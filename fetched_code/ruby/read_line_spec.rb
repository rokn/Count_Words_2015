# encoding: utf-8

RSpec.describe TTY::Prompt::Reader, '#read_line' do
  let(:input)  { StringIO.new }
  let(:output) { StringIO.new }

  subject(:reader) { described_class.new(input, output) }

  it 'masks characters' do
    mask = '*'
    input << "password\n"
    input.rewind
    answer = reader.read_line(mask)
    expect(answer).to eq("password")
  end

  it "echoes characters back" do
    input << "password\n"
    input.rewind
    answer = reader.read_line
    expect(answer).to eq("password")
    expect(output.string).to eq("")
  end

  it 'deletes characters when backspace pressed' do
    input << "aa\ba\bcc"
    input.rewind
    answer = reader.read_line
    expect(answer).to eq('acc')
  end
end
